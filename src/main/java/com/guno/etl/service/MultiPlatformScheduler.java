// MultiPlatformScheduler.java - Unified Scheduler for Shopee + TikTok ETL
package com.guno.etl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MultiPlatformScheduler {

    private static final Logger log = LoggerFactory.getLogger(MultiPlatformScheduler.class);

    @Autowired
    private ShopeeEtlService shopeeEtlService;

    @Autowired
    private TikTokEtlService tiktokEtlService;

    @Autowired
    private ShopeeApiService shopeeApiService;

    @Autowired
    private TikTokApiService tiktokApiService;

    // Platform enable/disable flags
    @Value("${etl.platforms.shopee.enabled:false}")
    private boolean shopeeEnabled;

    @Value("${etl.platforms.tiktok.enabled}")
    private boolean tiktokEnabled;

    @Value("${etl.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${etl.scheduler.max-consecutive-failures:5}")
    private int maxConsecutiveFailures;

    // Statistics tracking
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong shopeeSuccessCount = new AtomicLong(0);
    private final AtomicLong shopeeFailureCount = new AtomicLong(0);
    private final AtomicLong tiktokSuccessCount = new AtomicLong(0);
    private final AtomicLong tiktokFailureCount = new AtomicLong(0);
    private final AtomicLong shopeeConsecutiveFailures = new AtomicLong(0);
    private final AtomicLong tiktokConsecutiveFailures = new AtomicLong(0);

    private LocalDateTime lastSuccessTime;
    private LocalDateTime lastFailureTime;
    private String lastErrorMessage;

    // ===== MAIN SCHEDULER METHOD =====

    /**
     * Main scheduler method - runs every 30 seconds
     */
    @Scheduled(fixedRateString = "${etl.scheduler.fixed-rate:30000}",
            initialDelayString = "${etl.scheduler.initial-delay:10000}")
    public void processAllPlatforms() {
        if (!schedulerEnabled) {
            log.debug("Multi-platform scheduler is disabled");
            return;
        }

        long executionNumber = executionCount.incrementAndGet();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("=== Multi-Platform ETL Cycle #{} Started ===", executionNumber);

        boolean anySuccess = false;

        // Process Shopee platform
        if (shopeeEnabled) {
            boolean shopeeSuccess = processShopeeOrders();
            anySuccess = anySuccess || shopeeSuccess;
        } else {
            log.debug("Shopee platform is disabled, skipping");
        }

        // Process TikTok platform
        if (tiktokEnabled) {
            boolean tiktokSuccess = processTikTokOrders();
            anySuccess = anySuccess || tiktokSuccess;
        } else {
            log.debug("TikTok platform is disabled, skipping");
        }

        // Update global success/failure tracking
        if (anySuccess) {
            lastSuccessTime = startTime;
        } else {
            lastFailureTime = startTime;
            lastErrorMessage = "All enabled platforms failed";
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        log.info("=== Multi-Platform ETL Cycle #{} Completed in {} ms ===",
                executionNumber, durationMs);

        // Log summary statistics every 10 cycles
        if (executionNumber % 10 == 0) {
            logStatisticsSummary();
        }
    }

    // ===== PLATFORM-SPECIFIC PROCESSING =====

    /**
     * Process Shopee orders with error handling
     */
    private boolean processShopeeOrders() {
        try {
            log.debug("Processing Shopee orders...");

            ShopeeEtlService.EtlResult result = shopeeEtlService.processUpdatedOrders();

            if (result.isSuccess()) {
                shopeeSuccessCount.incrementAndGet();
                shopeeConsecutiveFailures.set(0);

                log.info("Shopee ETL SUCCESS: {}/{} orders processed in {} ms",
                        result.getOrdersProcessed(), result.getTotalOrders(), result.getDurationMs());

                return true;
            } else {
                handleShopeeFailure("ETL process failed: " + result.getErrorMessage());
                return false;
            }

        } catch (Exception e) {
            handleShopeeFailure("Exception during Shopee ETL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process TikTok orders with error handling
     */
    private boolean processTikTokOrders() {
        try {
            log.debug("Processing TikTok orders...");

            TikTokEtlService.EtlResult result = tiktokEtlService.processUpdatedOrders();

            if (result.isSuccess()) {
                tiktokSuccessCount.incrementAndGet();
                tiktokConsecutiveFailures.set(0);

                log.info("TikTok ETL SUCCESS: {}/{} orders processed in {} ms",
                        result.getOrdersProcessed(), result.getTotalOrders(), result.getDurationMs());

                return true;
            } else {
                handleTikTokFailure("ETL process failed: " + result.getErrorMessage());
                return false;
            }

        } catch (Exception e) {
            handleTikTokFailure("Exception during TikTok ETL: " + e.getMessage());
            return false;
        }
    }

    // ===== FAILURE HANDLING =====

    private void handleShopeeFailure(String errorMessage) {
        shopeeFailureCount.incrementAndGet();
        long consecutiveFailures = shopeeConsecutiveFailures.incrementAndGet();

        log.error("Shopee ETL FAILED (consecutive failures: {}): {}", consecutiveFailures, errorMessage);

        if (consecutiveFailures >= maxConsecutiveFailures) {
            log.error("🚨 ALERT: Shopee has {} consecutive failures! Consider investigating.", consecutiveFailures);
        }
    }

    private void handleTikTokFailure(String errorMessage) {
        tiktokFailureCount.incrementAndGet();
        long consecutiveFailures = tiktokConsecutiveFailures.incrementAndGet();

        log.error("TikTok ETL FAILED (consecutive failures: {}): {}", consecutiveFailures, errorMessage);

        if (consecutiveFailures >= maxConsecutiveFailures) {
            log.error("🚨 ALERT: TikTok has {} consecutive failures! Consider investigating.", consecutiveFailures);
        }
    }

    // ===== HEALTH CHECK SCHEDULER =====

    /**
     * Health check scheduler - runs every 5 minutes
     */
    @Scheduled(fixedRateString = "${etl.scheduler.health-check-interval:300000}")
    public void performHealthCheck() {
        if (!schedulerEnabled) {
            return;
        }

        log.info("=== Multi-Platform Health Check ===");

        // Check Shopee API health
        if (shopeeEnabled) {
            boolean shopeeHealthy = shopeeApiService.isApiHealthy();
            log.info("Shopee API Health: {}", shopeeHealthy ? "HEALTHY" : "UNHEALTHY");
        }

        // Check TikTok API health
        if (tiktokEnabled) {
            boolean tiktokHealthy = tiktokApiService.isApiHealthy();
            log.info("TikTok API Health: {}", tiktokHealthy ? "HEALTHY" : "UNHEALTHY");
        }

        logStatisticsSummary();
    }

    // ===== MANUAL CONTROL METHODS =====

    /**
     * Manually trigger ETL for all enabled platforms
     */
    public MultiPlatformEtlResult triggerManualEtl() {
        log.info("Manual ETL trigger initiated");

        MultiPlatformEtlResult result = new MultiPlatformEtlResult();
        result.setStartTime(LocalDateTime.now());

        // Process Shopee if enabled
        if (shopeeEnabled) {
            try {
                ShopeeEtlService.EtlResult shopeeResult = shopeeEtlService.processUpdatedOrders();
                result.setShopeeResult(shopeeResult);
                result.setShopeeSuccess(shopeeResult.isSuccess());
            } catch (Exception e) {
                result.setShopeeSuccess(false);
                result.setShopeeError("Shopee ETL failed: " + e.getMessage());
            }
        }

        // Process TikTok if enabled
        if (tiktokEnabled) {
            try {
                TikTokEtlService.EtlResult tiktokResult = tiktokEtlService.processUpdatedOrders();
                result.setTiktokResult(tiktokResult);
                result.setTiktokSuccess(tiktokResult.isSuccess());
            } catch (Exception e) {
                result.setTiktokSuccess(false);
                result.setTiktokError("TikTok ETL failed: " + e.getMessage());
            }
        }

        result.setEndTime(LocalDateTime.now());
        result.calculateDuration();

        log.info("Manual ETL completed: Shopee={}, TikTok={}, Duration={}ms",
                result.isShopeeSuccess(), result.isTiktokSuccess(), result.getDurationMs());

        return result;
    }

    /**
     * Enable/disable scheduler
     */
    public void enableScheduler() {
        this.schedulerEnabled = true;
        log.info("Multi-platform scheduler ENABLED");
    }

    public void disableScheduler() {
        this.schedulerEnabled = false;
        log.info("Multi-platform scheduler DISABLED");
    }

    /**
     * Reset statistics
     */
    public void resetStatistics() {
        executionCount.set(0);
        shopeeSuccessCount.set(0);
        shopeeFailureCount.set(0);
        tiktokSuccessCount.set(0);
        tiktokFailureCount.set(0);
        shopeeConsecutiveFailures.set(0);
        tiktokConsecutiveFailures.set(0);
        lastSuccessTime = null;
        lastFailureTime = null;
        lastErrorMessage = null;

        log.info("Multi-platform scheduler statistics reset");
    }

    // ===== STATISTICS AND MONITORING =====

    private void logStatisticsSummary() {
        log.info("=== Multi-Platform Scheduler Statistics ===");
        log.info("Total Executions: {}", executionCount.get());
        log.info("Shopee - Success: {}, Failures: {}, Consecutive Failures: {}",
                shopeeSuccessCount.get(), shopeeFailureCount.get(), shopeeConsecutiveFailures.get());
        log.info("TikTok - Success: {}, Failures: {}, Consecutive Failures: {}",
                tiktokSuccessCount.get(), tiktokFailureCount.get(), tiktokConsecutiveFailures.get());

        // Calculate success rates
        long totalShopee = shopeeSuccessCount.get() + shopeeFailureCount.get();
        long totalTikTok = tiktokSuccessCount.get() + tiktokFailureCount.get();

        double shopeeSuccessRate = totalShopee > 0 ? (double) shopeeSuccessCount.get() / totalShopee * 100.0 : 0.0;
        double tiktokSuccessRate = totalTikTok > 0 ? (double) tiktokSuccessCount.get() / totalTikTok * 100.0 : 0.0;

        log.info("Success Rates - Shopee: {:.1f}%, TikTok: {:.1f}%", shopeeSuccessRate, tiktokSuccessRate);

        if (lastSuccessTime != null) {
            log.info("Last Success: {}", lastSuccessTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        if (lastFailureTime != null) {
            log.info("Last Failure: {} - {}",
                    lastFailureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    lastErrorMessage);
        }
    }

    /**
     * Get current scheduler statistics
     */
    public MultiPlatformStatistics getStatistics() {
        MultiPlatformStatistics stats = new MultiPlatformStatistics();

        stats.setExecutionCount(executionCount.get());
        stats.setShopeeSuccessCount(shopeeSuccessCount.get());
        stats.setShopeeFailureCount(shopeeFailureCount.get());
        stats.setTiktokSuccessCount(tiktokSuccessCount.get());
        stats.setTiktokFailureCount(tiktokFailureCount.get());
        stats.setShopeeConsecutiveFailures(shopeeConsecutiveFailures.get());
        stats.setTiktokConsecutiveFailures(tiktokConsecutiveFailures.get());
        stats.setLastSuccessTime(lastSuccessTime);
        stats.setLastFailureTime(lastFailureTime);
        stats.setLastErrorMessage(lastErrorMessage);
        stats.setSchedulerEnabled(schedulerEnabled);
        stats.setShopeeEnabled(shopeeEnabled);
        stats.setTiktokEnabled(tiktokEnabled);

        return stats;
    }

    // ===== RESULT CLASSES =====

    public static class MultiPlatformEtlResult {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long durationMs;

        private boolean shopeeSuccess;
        private boolean tiktokSuccess;
        private String shopeeError;
        private String tiktokError;

        private ShopeeEtlService.EtlResult shopeeResult;
        private TikTokEtlService.EtlResult tiktokResult;

        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public long getDurationMs() { return durationMs; }

        public void calculateDuration() {
            if (startTime != null && endTime != null) {
                this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            }
        }

        public boolean isShopeeSuccess() { return shopeeSuccess; }
        public void setShopeeSuccess(boolean shopeeSuccess) { this.shopeeSuccess = shopeeSuccess; }

        public boolean isTiktokSuccess() { return tiktokSuccess; }
        public void setTiktokSuccess(boolean tiktokSuccess) { this.tiktokSuccess = tiktokSuccess; }

        public String getShopeeError() { return shopeeError; }
        public void setShopeeError(String shopeeError) { this.shopeeError = shopeeError; }

        public String getTiktokError() { return tiktokError; }
        public void setTiktokError(String tiktokError) { this.tiktokError = tiktokError; }

        public ShopeeEtlService.EtlResult getShopeeResult() { return shopeeResult; }
        public void setShopeeResult(ShopeeEtlService.EtlResult shopeeResult) { this.shopeeResult = shopeeResult; }

        public TikTokEtlService.EtlResult getTiktokResult() { return tiktokResult; }
        public void setTiktokResult(TikTokEtlService.EtlResult tiktokResult) { this.tiktokResult = tiktokResult; }

        public boolean isOverallSuccess() {
            return shopeeSuccess || tiktokSuccess; // Success if at least one platform succeeds
        }
    }

    public static class MultiPlatformStatistics {
        private long executionCount;
        private long shopeeSuccessCount;
        private long shopeeFailureCount;
        private long tiktokSuccessCount;
        private long tiktokFailureCount;
        private long shopeeConsecutiveFailures;
        private long tiktokConsecutiveFailures;
        private LocalDateTime lastSuccessTime;
        private LocalDateTime lastFailureTime;
        private String lastErrorMessage;
        private boolean schedulerEnabled;
        private boolean shopeeEnabled;
        private boolean tiktokEnabled;

        // Getters and setters
        public long getExecutionCount() { return executionCount; }
        public void setExecutionCount(long executionCount) { this.executionCount = executionCount; }

        public long getShopeeSuccessCount() { return shopeeSuccessCount; }
        public void setShopeeSuccessCount(long shopeeSuccessCount) { this.shopeeSuccessCount = shopeeSuccessCount; }

        public long getShopeeFailureCount() { return shopeeFailureCount; }
        public void setShopeeFailureCount(long shopeeFailureCount) { this.shopeeFailureCount = shopeeFailureCount; }

        public long getTiktokSuccessCount() { return tiktokSuccessCount; }
        public void setTiktokSuccessCount(long tiktokSuccessCount) { this.tiktokSuccessCount = tiktokSuccessCount; }

        public long getTiktokFailureCount() { return tiktokFailureCount; }
        public void setTiktokFailureCount(long tiktokFailureCount) { this.tiktokFailureCount = tiktokFailureCount; }

        public long getShopeeConsecutiveFailures() { return shopeeConsecutiveFailures; }
        public void setShopeeConsecutiveFailures(long shopeeConsecutiveFailures) { this.shopeeConsecutiveFailures = shopeeConsecutiveFailures; }

        public long getTiktokConsecutiveFailures() { return tiktokConsecutiveFailures; }
        public void setTiktokConsecutiveFailures(long tiktokConsecutiveFailures) { this.tiktokConsecutiveFailures = tiktokConsecutiveFailures; }

        public LocalDateTime getLastSuccessTime() { return lastSuccessTime; }
        public void setLastSuccessTime(LocalDateTime lastSuccessTime) { this.lastSuccessTime = lastSuccessTime; }

        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
        public void setLastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; }

        public String getLastErrorMessage() { return lastErrorMessage; }
        public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }

        public boolean isSchedulerEnabled() { return schedulerEnabled; }
        public void setSchedulerEnabled(boolean schedulerEnabled) { this.schedulerEnabled = schedulerEnabled; }

        public boolean isShopeeEnabled() { return shopeeEnabled; }
        public void setShopeeEnabled(boolean shopeeEnabled) { this.shopeeEnabled = shopeeEnabled; }

        public boolean isTiktokEnabled() { return tiktokEnabled; }
        public void setTiktokEnabled(boolean tiktokEnabled) { this.tiktokEnabled = tiktokEnabled; }

        public double getShopeeSuccessRate() {
            long total = shopeeSuccessCount + shopeeFailureCount;
            return total > 0 ? (double) shopeeSuccessCount / total * 100.0 : 0.0;
        }

        public double getTiktokSuccessRate() {
            long total = tiktokSuccessCount + tiktokFailureCount;
            return total > 0 ? (double) tiktokSuccessCount / total * 100.0 : 0.0;
        }
    }
}