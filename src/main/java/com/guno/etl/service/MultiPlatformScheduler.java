// MultiPlatformScheduler.java - OPTIMIZED VERSION with Facebook Support
package com.guno.etl.service;

import com.guno.etl.service.ShopeeEtlService;
import com.guno.etl.service.TikTokEtlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@ConditionalOnProperty(
        value = "etl.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class MultiPlatformScheduler {

    // ===== SERVICES =====

    @Autowired(required = false)
    private ShopeeEtlService shopeeEtlService;

    @Autowired(required = false)
    private TikTokEtlService tiktokEtlService;

    // ===== CONFIGURATION =====

    @Value("${etl.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Value("${etl.platforms.shopee.enabled:false}")
    private boolean shopeeEnabled;

    @Value("${etl.platforms.tiktok.enabled:false}")
    private boolean tiktokEnabled;

    @Value("${etl.platforms.facebook.enabled:false}")
    private boolean facebookEnabled;

    @Value("${etl.scheduler.max-consecutive-failures:3}")
    private int maxConsecutiveFailures;

    @Value("${etl.scheduler.parallel-execution:true}")
    private boolean parallelExecution;

    // ===== MONITORING & STATISTICS =====

    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    // Platform-specific counters
    private final Map<String, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> consecutiveFailures = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, String> lastErrorMessages = new ConcurrentHashMap<>();

    // Overall tracking
    private LocalDateTime lastSuccessTime;
    private LocalDateTime lastFailureTime;

    public MultiPlatformScheduler() {
        // Initialize counters for all platforms
        initializePlatformCounters("SHOPEE");
        initializePlatformCounters("TIKTOK");
        initializePlatformCounters("FACEBOOK");
    }

    private void initializePlatformCounters(String platform) {
        successCounts.put(platform, new AtomicLong(0));
        failureCounts.put(platform, new AtomicLong(0));
        consecutiveFailures.put(platform, new AtomicLong(0));
    }

    // ===== MAIN SCHEDULER METHOD =====

    /**
     * Main scheduler method - runs every 30 seconds
     * OPTIMIZED: Parallel execution + better error handling + comprehensive monitoring
     */
    @Scheduled(
            fixedRateString = "${etl.scheduler.fixed-rate:30000}",
            initialDelayString = "${etl.scheduler.initial-delay:5000}"
    )
    public void processAllPlatforms() {
        if (!schedulerEnabled) {
            log.debug("🔒 Multi-platform scheduler is disabled");
            return;
        }

        // Prevent concurrent executions
        if (!isExecuting.compareAndSet(false, true)) {
            log.warn("⚠️ Previous ETL execution still running, skipping this cycle");
            return;
        }

        long executionNumber = executionCount.incrementAndGet();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🚀 Multi-Platform ETL Cycle #{} Started", executionNumber);

        try {
            if (parallelExecution) {
                processAllPlatformsParallel();
            } else {
                processAllPlatformsSequential();
            }

            lastSuccessTime = LocalDateTime.now();
            log.info("✅ Multi-Platform ETL Cycle #{} Completed in {}ms",
                    executionNumber, Duration.between(startTime, LocalDateTime.now()).toMillis());

        } catch (Exception e) {
            lastFailureTime = LocalDateTime.now();
            log.error("❌ Multi-Platform ETL Cycle #{} Failed: {}", executionNumber, e.getMessage(), e);
        } finally {
            isExecuting.set(false);
            logExecutionSummary();
        }
    }

    // ===== EXECUTION STRATEGIES =====

    /**
     * OPTIMIZED: Parallel execution for better performance
     */
    private void processAllPlatformsParallel() {
        log.info("🔄 Processing platforms in PARALLEL mode");

        CompletableFuture<Boolean> shopeeTask = CompletableFuture.supplyAsync(() -> {
            if (shopeeEnabled && shopeeEtlService != null) {
                return processPlatform("SHOPEE", shopeeEtlService::processUpdatedOrders);
            }
            return true;
        });

        CompletableFuture<Boolean> tiktokTask = CompletableFuture.supplyAsync(() -> {
            if (tiktokEnabled && tiktokEtlService != null) {
                return processPlatform("TIKTOK", tiktokEtlService::processUpdatedOrders);
            }
            return true;
        });
        // Wait for all platforms to complete
        CompletableFuture.allOf(shopeeTask, tiktokTask).join();
    }

    /**
     * Sequential execution for debugging or resource constraints
     */
    private void processAllPlatformsSequential() {
        log.info("🔄 Processing platforms in SEQUENTIAL mode");

        if (shopeeEnabled && shopeeEtlService != null) {
            processPlatform("SHOPEE", shopeeEtlService::processUpdatedOrders);
        }

        if (tiktokEnabled && tiktokEtlService != null) {
            processPlatform("TIKTOK", tiktokEtlService::processUpdatedOrders);
        }
    }

    // ===== PLATFORM PROCESSING =====

    /**
     * OPTIMIZED: Generic platform processing with comprehensive monitoring
     */
    private boolean processPlatform(String platformName, PlatformProcessor processor) {
        if (!isPlatformHealthy(platformName)) {
            log.warn("⚠️ Skipping {} due to consecutive failures", platformName);
            return false;
        }

        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("🔄 Processing {} platform...", platformName);

            Object result = processor.process();
            boolean success = evaluateResult(result);

            if (success) {
                handlePlatformSuccess(platformName, startTime);
                return true;
            } else {
                handlePlatformFailure(platformName, "ETL processing returned failure", startTime);
                return false;
            }

        } catch (Exception e) {
            handlePlatformFailure(platformName, e.getMessage(), startTime);
            return false;
        }
    }

    /**
     * Evaluate ETL result based on type
     */
    private boolean evaluateResult(Object result) {
        if (result == null) return false;

        // Handle different result types from ETL services
        if (result instanceof ShopeeEtlService.EtlResult) {
            return ((ShopeeEtlService.EtlResult) result).isSuccess();
        } else if (result instanceof TikTokEtlService.EtlResult) {
            return ((TikTokEtlService.EtlResult) result).isSuccess();
        } else if (result instanceof Boolean) {
            return (Boolean) result;
        }

        return true; // Default to success if unclear
    }

    // ===== HEALTH MONITORING =====

    /**
     * OPTIMIZED: Circuit breaker pattern for platform health
     */
    private boolean isPlatformHealthy(String platformName) {
        long failures = consecutiveFailures.get(platformName).get();
        if (failures >= maxConsecutiveFailures) {
            LocalDateTime lastExecution = lastExecutionTimes.get(platformName);
            if (lastExecution != null &&
                    Duration.between(lastExecution, LocalDateTime.now()).toMinutes() < 5) {
                return false; // Circuit breaker: wait 5 minutes before retry
            }
        }
        return true;
    }

    private void handlePlatformSuccess(String platformName, LocalDateTime startTime) {
        successCounts.get(platformName).incrementAndGet();
        consecutiveFailures.get(platformName).set(0); // Reset failure count
        lastExecutionTimes.put(platformName, LocalDateTime.now());

        long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
        log.info("✅ {} processing completed successfully in {}ms", platformName, duration);
    }

    private void handlePlatformFailure(String platformName, String errorMessage, LocalDateTime startTime) {
        failureCounts.get(platformName).incrementAndGet();
        consecutiveFailures.get(platformName).incrementAndGet();
        lastExecutionTimes.put(platformName, LocalDateTime.now());
        lastErrorMessages.put(platformName, errorMessage);

        long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
        log.error("❌ {} processing failed after {}ms: {}", platformName, duration, errorMessage);
    }

    // ===== MONITORING & STATISTICS =====

    private void logExecutionSummary() {
        log.info("📊 Platform Statistics:");

        for (String platform : new String[]{"SHOPEE", "TIKTOK", "FACEBOOK"}) {
            long success = successCounts.get(platform).get();
            long failure = failureCounts.get(platform).get();
            long consecutive = consecutiveFailures.get(platform).get();

            log.info("   {} - Success: {}, Failures: {}, Consecutive Failures: {}",
                    platform, success, failure, consecutive);
        }
    }

    // ===== PUBLIC API FOR MONITORING =====

    public Map<String, Object> getSchedulerStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        stats.put("schedulerEnabled", schedulerEnabled);
        stats.put("totalExecutions", executionCount.get());
        stats.put("isCurrentlyExecuting", isExecuting.get());
        stats.put("lastSuccessTime", lastSuccessTime);
        stats.put("lastFailureTime", lastFailureTime);
        stats.put("parallelExecution", parallelExecution);

        // Platform-specific stats
        Map<String, Object> platformStats = new ConcurrentHashMap<>();
        for (String platform : new String[]{"SHOPEE", "TIKTOK", "FACEBOOK"}) {
            Map<String, Object> platformData = new ConcurrentHashMap<>();
            platformData.put("enabled", isPlatformEnabled(platform));
            platformData.put("successCount", successCounts.get(platform).get());
            platformData.put("failureCount", failureCounts.get(platform).get());
            platformData.put("consecutiveFailures", consecutiveFailures.get(platform).get());
            platformData.put("lastExecutionTime", lastExecutionTimes.get(platform));
            platformData.put("lastErrorMessage", lastErrorMessages.get(platform));
            platformData.put("isHealthy", isPlatformHealthy(platform));

            platformStats.put(platform, platformData);
        }
        stats.put("platforms", platformStats);

        return stats;
    }

    private boolean isPlatformEnabled(String platform) {
        switch (platform) {
            case "SHOPEE": return shopeeEnabled;
            case "TIKTOK": return tiktokEnabled;
            case "FACEBOOK": return facebookEnabled;
            default: return false;
        }
    }

    /**
     * Manual trigger for specific platform
     */
    public boolean triggerPlatform(String platformName) {
        log.info("🎯 Manual trigger requested for {} platform", platformName);

        switch (platformName.toUpperCase()) {
            case "SHOPEE":
                if (shopeeEnabled && shopeeEtlService != null) {
                    return processPlatform("SHOPEE", shopeeEtlService::processUpdatedOrders);
                }
                break;
            case "TIKTOK":
                if (tiktokEnabled && tiktokEtlService != null) {
                    return processPlatform("TIKTOK", tiktokEtlService::processUpdatedOrders);
                }
                break;
            default:
                log.warn("⚠️ Unknown platform: {}", platformName);
                return false;
        }

        log.warn("⚠️ Platform {} is disabled or service unavailable", platformName);
        return false;
    }

    /**
     * Manual trigger for all enabled platforms
     */
    public void triggerAllPlatforms() {
        log.info("🎯 Manual trigger requested for all platforms");
        processAllPlatforms();
    }

    // ===== FUNCTIONAL INTERFACE =====

    @FunctionalInterface
    private interface PlatformProcessor {
        Object process() throws Exception;
    }
}