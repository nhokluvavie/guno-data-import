// ScheduledEtlService.java - 30-second ETL Scheduler
package com.guno.etl.service;

import com.guno.etl.service.ShopeeEtlService.EtlResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ScheduledEtlService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEtlService.class);

    @Autowired
    private ShopeeEtlService etlService;

    @Autowired
    private ShopeeApiService apiService;

    // Configuration properties
    @Value("${etl.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${etl.scheduler.fixed-rate:30000}")
    private long fixedRateMs;

    @Value("${etl.scheduler.initial-delay:10000}")
    private long initialDelayMs;

    @Value("${etl.scheduler.health-check-interval:300000}")
    private long healthCheckIntervalMs; // 5 minutes

    @Value("${etl.scheduler.max-consecutive-failures:5}")
    private int maxConsecutiveFailures;

    // Runtime tracking
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private LocalDateTime lastSuccessTime;
    private LocalDateTime lastFailureTime;
    private String lastErrorMessage;

    // ===== MAIN SCHEDULED METHOD =====

    /**
     * Main scheduled ETL process - runs every 30 seconds
     */
    @Scheduled(fixedRateString = "${etl.scheduler.fixed-rate:30000}",
            initialDelayString = "${etl.scheduler.initial-delay:10000}")
    public void scheduledEtlProcess() {
        if (!schedulerEnabled) {
            log.debug("ETL scheduler is disabled");
            return;
        }

        if (isRunning.get()) {
            log.warn("ETL process is already running, skipping this cycle");
            return;
        }

        long executionNumber = executionCount.incrementAndGet();
        log.debug("=== ETL Cycle #{} Started ===", executionNumber);

        try {
            isRunning.set(true);

            // Check API health before processing
            if (!isApiHealthy()) {
                log.warn("API health check failed, skipping ETL cycle");
                recordFailure("API health check failed");
                return;
            }

            // Run ETL process
            EtlResult result = etlService.processUpdatedOrders();

            // Process results
            if (result.isSuccess()) {
                recordSuccess(result);
                log.info("ETL Cycle #{} completed successfully - Processed: {}/{}, Duration: {}ms",
                        executionNumber, result.getOrdersProcessed(), result.getTotalOrders(), result.getDurationMs());

                if (result.getFailedOrders().size() > 0) {
                    log.warn("ETL Cycle #{} - {} orders failed", executionNumber, result.getFailedOrders().size());
                }
            } else {
                recordFailure(result.getErrorMessage());
                log.error("ETL Cycle #{} failed: {}", executionNumber, result.getErrorMessage());
            }

        } catch (Exception e) {
            recordFailure("Scheduler exception: " + e.getMessage());
            log.error("ETL Cycle #{} failed with exception: {}", executionNumber, e.getMessage(), e);
        } finally {
            isRunning.set(false);
            log.debug("=== ETL Cycle #{} Ended ===", executionNumber);
        }
    }

    // ===== HEALTH CHECK SCHEDULER =====

    /**
     * Periodic health check and statistics logging
     */
    @Scheduled(fixedRateString = "${etl.scheduler.health-check-interval:300000}")
    public void healthCheckAndStats() {
        if (!schedulerEnabled) {
            return;
        }

        log.info("=== ETL Scheduler Health Check ===");
        log.info("Scheduler Status: {}", schedulerEnabled ? "ENABLED" : "DISABLED");
        log.info("Total Executions: {}", executionCount.get());
        log.info("Success Count: {}", successCount.get());
        log.info("Failure Count: {}", failureCount.get());
        log.info("Success Rate: {:.1f}%", calculateSuccessRate());
        log.info("Consecutive Failures: {}", consecutiveFailures.get());
        log.info("Last Success: {}", lastSuccessTime != null ? lastSuccessTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "Never");

        if (lastFailureTime != null) {
            log.info("Last Failure: {} - {}",
                    lastFailureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    lastErrorMessage);
        }

        // Check for concerning patterns
        if (consecutiveFailures.get() >= maxConsecutiveFailures) {
            log.error("ALERT: {} consecutive failures detected! Manual intervention may be required.",
                    consecutiveFailures.get());
        }

        // API health check
        boolean apiHealthy = isApiHealthy();
        log.info("API Health: {}", apiHealthy ? "HEALTHY" : "UNHEALTHY");

        if (!apiHealthy) {
            log.warn("API is not responding properly. ETL cycles may fail.");
        }
    }

    // ===== APPLICATION STARTUP HANDLER =====

    /**
     * Initialize scheduler when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== ETL Scheduler Initialization ===");
        log.info("Scheduler Enabled: {}", schedulerEnabled);
        log.info("Fixed Rate: {} ms ({} seconds)", fixedRateMs, fixedRateMs / 1000);
        log.info("Initial Delay: {} ms ({} seconds)", initialDelayMs, initialDelayMs / 1000);
        log.info("Health Check Interval: {} ms ({} minutes)", healthCheckIntervalMs, healthCheckIntervalMs / 60000);
        log.info("Max Consecutive Failures: {}", maxConsecutiveFailures);

        if (schedulerEnabled) {
            log.info("ETL Scheduler will start in {} seconds...", initialDelayMs / 1000);

            // Test API connectivity at startup
            try {
                String connectionTest = apiService.testConnection();
                log.info("API Connection Test: {}", connectionTest);
            } catch (Exception e) {
                log.warn("API Connection Test failed at startup: {}", e.getMessage());
            }
        } else {
            log.warn("ETL Scheduler is DISABLED. Set etl.scheduler.enabled=true to enable.");
        }
    }

    // ===== MANUAL CONTROL METHODS =====

    /**
     * Manually trigger ETL process (for testing or emergency)
     */
    public EtlResult triggerManualEtl() {
        log.info("Manual ETL trigger requested");

        if (isRunning.get()) {
            log.warn("ETL is already running, cannot trigger manual execution");
            EtlResult result = new EtlResult();
            result.setSuccess(false);
            result.setErrorMessage("ETL process is already running");
            return result;
        }

        try {
            isRunning.set(true);
            EtlResult result = etlService.processUpdatedOrders();

            if (result.isSuccess()) {
                log.info("Manual ETL completed successfully - Processed: {}/{}",
                        result.getOrdersProcessed(), result.getTotalOrders());
            } else {
                log.error("Manual ETL failed: {}", result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("Manual ETL failed with exception: {}", e.getMessage(), e);
            EtlResult result = new EtlResult();
            result.setSuccess(false);
            result.setErrorMessage("Manual ETL exception: " + e.getMessage());
            return result;
        } finally {
            isRunning.set(false);
        }
    }

    /**
     * Enable scheduler
     */
    public void enableScheduler() {
        schedulerEnabled = true;
        log.info("ETL Scheduler ENABLED");
    }

    /**
     * Disable scheduler
     */
    public void disableScheduler() {
        schedulerEnabled = false;
        log.warn("ETL Scheduler DISABLED");
    }

    /**
     * Reset statistics
     */
    public void resetStatistics() {
        executionCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        consecutiveFailures.set(0);
        lastSuccessTime = null;
        lastFailureTime = null;
        lastErrorMessage = null;
        log.info("ETL Scheduler statistics reset");
    }

    // ===== HELPER METHODS =====

    private void recordSuccess(EtlResult result) {
        successCount.incrementAndGet();
        consecutiveFailures.set(0);
        lastSuccessTime = LocalDateTime.now();
        lastErrorMessage = null;
    }

    private void recordFailure(String errorMessage) {
        failureCount.incrementAndGet();
        consecutiveFailures.incrementAndGet();
        lastFailureTime = LocalDateTime.now();
        lastErrorMessage = errorMessage;
    }

    private double calculateSuccessRate() {
        long total = executionCount.get();
        if (total == 0) return 0.0;
        return (double) successCount.get() / total * 100.0;
    }

    private boolean isApiHealthy() {
        try {
            return apiService.isApiHealthy();
        } catch (Exception e) {
            log.warn("API health check exception: {}", e.getMessage());
            return false;
        }
    }

    // ===== GETTERS FOR MONITORING =====

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public boolean isCurrentlyRunning() {
        return isRunning.get();
    }

    public long getExecutionCount() {
        return executionCount.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getFailureCount() {
        return failureCount.get();
    }

    public long getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public double getSuccessRate() {
        return calculateSuccessRate();
    }

    public LocalDateTime getLastSuccessTime() {
        return lastSuccessTime;
    }

    public LocalDateTime getLastFailureTime() {
        return lastFailureTime;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public long getFixedRateMs() {
        return fixedRateMs;
    }

    // ===== SCHEDULER STATUS CLASS =====

    public static class SchedulerStatus {
        private boolean enabled;
        private boolean currentlyRunning;
        private long executionCount;
        private long successCount;
        private long failureCount;
        private double successRate;
        private long consecutiveFailures;
        private LocalDateTime lastSuccessTime;
        private LocalDateTime lastFailureTime;
        private String lastErrorMessage;
        private long fixedRateMs;
        private boolean apiHealthy;

        // Constructor
        public SchedulerStatus(ScheduledEtlService scheduler, boolean apiHealthy) {
            this.enabled = scheduler.isSchedulerEnabled();
            this.currentlyRunning = scheduler.isCurrentlyRunning();
            this.executionCount = scheduler.getExecutionCount();
            this.successCount = scheduler.getSuccessCount();
            this.failureCount = scheduler.getFailureCount();
            this.successRate = scheduler.getSuccessRate();
            this.consecutiveFailures = scheduler.getConsecutiveFailures();
            this.lastSuccessTime = scheduler.getLastSuccessTime();
            this.lastFailureTime = scheduler.getLastFailureTime();
            this.lastErrorMessage = scheduler.getLastErrorMessage();
            this.fixedRateMs = scheduler.getFixedRateMs();
            this.apiHealthy = apiHealthy;
        }

        // Getters
        public boolean isEnabled() { return enabled; }
        public boolean isCurrentlyRunning() { return currentlyRunning; }
        public long getExecutionCount() { return executionCount; }
        public long getSuccessCount() { return successCount; }
        public long getFailureCount() { return failureCount; }
        public double getSuccessRate() { return successRate; }
        public long getConsecutiveFailures() { return consecutiveFailures; }
        public LocalDateTime getLastSuccessTime() { return lastSuccessTime; }
        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
        public String getLastErrorMessage() { return lastErrorMessage; }
        public long getFixedRateMs() { return fixedRateMs; }
        public boolean isApiHealthy() { return apiHealthy; }

        public String getFixedRateSeconds() {
            return String.valueOf(fixedRateMs / 1000);
        }
    }

    /**
     * Get complete scheduler status
     */
    public SchedulerStatus getSchedulerStatus() {
        boolean apiHealthy = isApiHealthy();
        return new SchedulerStatus(this, apiHealthy);
    }
}