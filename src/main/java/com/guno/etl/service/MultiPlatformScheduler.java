// MultiPlatformScheduler.java - UPDATED to use ParallelApiService
package com.guno.etl.service;

import com.guno.etl.dto.ParallelProcessingResult;
import com.guno.etl.dto.EtlResult;
import com.guno.etl.dto.FailedOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
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

    // 🆕 NEW: Use ParallelApiService for optimized processing
    @Autowired
    private ParallelApiService parallelApiService;

    // 🆕 KEEP: Individual services for fallback (backward compatibility)
    @Autowired(required = false)
    private ShopeeEtlService shopeeEtlService;

    @Autowired(required = false)
    private TikTokEtlService tiktokEtlService;

    @Autowired(required = false)
    private FacebookEtlService facebookEtlService;

    // ===== CONFIGURATION =====

    @Value("${etl.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Value("${etl.scheduler.use-parallel-processing:true}")  // 🆕 NEW
    private boolean useParallelProcessing;

    @Value("${etl.platforms.shopee.enabled:false}")
    private boolean shopeeEnabled;

    @Value("${etl.platforms.tiktok.enabled:false}")
    private boolean tiktokEnabled;

    @Value("${etl.platforms.facebook.enabled:false}")
    private boolean facebookEnabled;

    @Value("${etl.scheduler.max-consecutive-failures:5}")
    private int maxConsecutiveFailures;

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
    private AtomicLong totalConsecutiveFailures = new AtomicLong(0);

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
     * 🆕 UPDATED: Main scheduler method using ParallelApiService
     * Runs every 30 seconds with parallel processing support
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
            ParallelProcessingResult result;

            if (useParallelProcessing && parallelApiService != null) {
                // 🆕 NEW: Use optimized parallel processing
                log.info("⚡ Using PARALLEL processing mode");
                result = parallelApiService.processAllPlatformsInParallel();
            } else {
                // 🆕 FALLBACK: Use legacy sequential processing
                log.info("🔄 Using SEQUENTIAL processing mode (fallback)");
                result = processAllPlatformsSequential();
            }

            // Update statistics based on results
            updateStatisticsFromResult(result);

            // Check overall success
            if (result.isSuccess()) {
                handleOverallSuccess(result, startTime, executionNumber);
            } else {
                handleOverallFailure(result, startTime, executionNumber);
            }

        } catch (Exception e) {
            handleUnexpectedException(e, startTime, executionNumber);
        } finally {
            isExecuting.set(false);
        }
    }

    // ===== PARALLEL PROCESSING (NEW) =====

    /**
     * 🆕 NEW: Process using ParallelApiService - this is the main optimization
     */
    private void handleOverallSuccess(ParallelProcessingResult result, LocalDateTime startTime, long executionNumber) {
        lastSuccessTime = LocalDateTime.now();
        totalConsecutiveFailures.set(0);

        long durationMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

        log.info("✅ Multi-Platform ETL Cycle #{} Completed Successfully", executionNumber);
        log.info("📊 Execution Summary: {}", result.getSummary());
        log.info("⏱️ Total Duration: {}ms", durationMs);

        if (result.isParallelEnabled()) {
            log.info("⚡ Performance Boost: PARALLEL execution saved significant time!");
        }
    }

    private void handleOverallFailure(ParallelProcessingResult result, LocalDateTime startTime, long executionNumber) {
        lastFailureTime = LocalDateTime.now();
        long failures = totalConsecutiveFailures.incrementAndGet();

        long durationMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

        log.error("❌ Multi-Platform ETL Cycle #{} Failed", executionNumber);
        log.error("📊 Execution Summary: {}", result.getSummary());
        log.error("⏱️ Total Duration: {}ms", durationMs);
        log.error("🔥 Consecutive Failures: {}/{}", failures, maxConsecutiveFailures);

        if (failures >= maxConsecutiveFailures) {
            log.error("🚨 CRITICAL: Maximum consecutive failures reached! Consider manual intervention.");
        }
    }

    // ===== SEQUENTIAL PROCESSING (FALLBACK) =====

    /**
     * 🆕 FALLBACK: Sequential processing when parallel is disabled
     */
    private ParallelProcessingResult processAllPlatformsSequential() {
        log.info("🔄 Processing platforms SEQUENTIALLY (fallback mode)");

        ParallelProcessingResult result = new ParallelProcessingResult();
        result.setStartTime(LocalDateTime.now());
        result.setParallelEnabled(false);

        try {
            // Process each platform sequentially
            if (shopeeEnabled && shopeeEtlService != null) {
                log.info("📱 Processing Shopee ETL...");
                EtlResult shopeeResult = shopeeEtlService.processUpdatedOrders();
                result.setShopeeResult(shopeeResult);
                updatePlatformStatistics("SHOPEE", shopeeResult);
            }

            if (tiktokEnabled && tiktokEtlService != null) {
                log.info("🎵 Processing TikTok ETL...");
                EtlResult tiktokResult = tiktokEtlService.processUpdatedOrders();
                result.setTiktokResult(tiktokResult);
                updatePlatformStatistics("TIKTOK", tiktokResult);
            }

            if (facebookEnabled && facebookEtlService != null) {
                log.info("📘 Processing Facebook ETL...");
                String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                EtlResult facebookResult = facebookEtlService.processOrdersForDate(todayDate);
                result.setFacebookResult(facebookResult);
                updatePlatformStatistics("FACEBOOK", facebookResult);
            }

            // Calculate summary
            calculateSummaryStatistics(result);
            result.setSuccess(result.hasAnySuccess());
            result.setEndTime(LocalDateTime.now());

            log.info("✅ SEQUENTIAL ETL processing completed");
            return result;

        } catch (Exception e) {
            log.error("❌ SEQUENTIAL ETL processing failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    // ===== STATISTICS & MONITORING =====

    /**
     * Update statistics from ParallelProcessingResult
     */
    private void updateStatisticsFromResult(ParallelProcessingResult result) {
        if (result.getShopeeResult() != null) {
            updatePlatformStatistics("SHOPEE", result.getShopeeResult());
        }

        if (result.getTiktokResult() != null) {
            updatePlatformStatistics("TIKTOK", result.getTiktokResult());
        }

        if (result.getFacebookResult() != null) {
            updatePlatformStatistics("FACEBOOK", result.getFacebookResult());
        }
    }

    /**
     * Update platform-specific statistics
     */
    private void updatePlatformStatistics(String platform, EtlResult result) {
        lastExecutionTimes.put(platform, LocalDateTime.now());

        if (result.isSuccess()) {
            successCounts.get(platform).incrementAndGet();
            consecutiveFailures.get(platform).set(0);
            lastErrorMessages.remove(platform);
            log.debug("✅ {} ETL succeeded: {}/{} orders processed",
                    platform, result.getOrdersProcessed(), result.getTotalOrders());
        } else {
            failureCounts.get(platform).incrementAndGet();
            consecutiveFailures.get(platform).incrementAndGet();
            lastErrorMessages.put(platform, result.getErrorMessage());
            log.error("❌ {} ETL failed: {}", platform, result.getErrorMessage());
        }
    }

    /**
     * Calculate summary statistics for ParallelProcessingResult
     */
    private void calculateSummaryStatistics(ParallelProcessingResult result) {
        int totalOrders = 0;
        int totalProcessed = 0;
        int totalFailed = 0;

        if (result.getShopeeResult() != null) {
            totalOrders += result.getShopeeResult().getTotalOrders();
            totalProcessed += result.getShopeeResult().getOrdersProcessed();
            totalFailed += result.getShopeeResult().getFailedOrders().size();
        }

        if (result.getTiktokResult() != null) {
            totalOrders += result.getTiktokResult().getTotalOrders();
            totalProcessed += result.getTiktokResult().getOrdersProcessed();
            totalFailed += result.getTiktokResult().getFailedOrders().size();
        }

        if (result.getFacebookResult() != null) {
            totalOrders += result.getFacebookResult().getTotalOrders();
            totalProcessed += result.getFacebookResult().getOrdersProcessed();
            totalFailed += result.getFacebookResult().getFailedOrders().size();
        }

        result.setTotalOrders(totalOrders);
        result.setTotalProcessed(totalProcessed);
        result.setTotalFailed(totalFailed);
    }

    /**
     * Handle unexpected exceptions
     */
    private void handleUnexpectedException(Exception e, LocalDateTime startTime, long executionNumber) {
        lastFailureTime = LocalDateTime.now();
        long failures = totalConsecutiveFailures.incrementAndGet();

        long durationMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

        log.error("💥 Multi-Platform ETL Cycle #{} CRASHED", executionNumber);
        log.error("💥 Unexpected Exception: {}", e.getMessage(), e);
        log.error("⏱️ Duration before crash: {}ms", durationMs);
        log.error("🔥 Consecutive Failures: {}/{}", failures, maxConsecutiveFailures);

        if (failures >= maxConsecutiveFailures) {
            log.error("🚨 CRITICAL: System may be unstable! Consider restarting application.");
        }
    }

    // ===== PUBLIC MONITORING METHODS =====

    /**
     * Get current execution statistics
     */
    public Map<String, Object> getExecutionStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        stats.put("totalExecutions", executionCount.get());
        stats.put("isCurrentlyExecuting", isExecuting.get());
        stats.put("lastSuccessTime", lastSuccessTime);
        stats.put("lastFailureTime", lastFailureTime);
        stats.put("consecutiveFailures", totalConsecutiveFailures.get());
        stats.put("maxConsecutiveFailures", maxConsecutiveFailures);
        stats.put("parallelProcessingEnabled", useParallelProcessing);

        // Platform statistics
        Map<String, Map<String, Object>> platformStats = new ConcurrentHashMap<>();
        for (String platform : successCounts.keySet()) {
            Map<String, Object> platformInfo = new ConcurrentHashMap<>();
            platformInfo.put("successCount", successCounts.get(platform).get());
            platformInfo.put("failureCount", failureCounts.get(platform).get());
            platformInfo.put("consecutiveFailures", consecutiveFailures.get(platform).get());
            platformInfo.put("lastExecution", lastExecutionTimes.get(platform));
            platformInfo.put("lastError", lastErrorMessages.get(platform));
            platformStats.put(platform, platformInfo);
        }
        stats.put("platformStatistics", platformStats);

        return stats;
    }

    /**
     * Reset all statistics
     */
    public void resetStatistics() {
        log.info("🔄 Resetting ETL scheduler statistics");
        executionCount.set(0);
        totalConsecutiveFailures.set(0);
        lastSuccessTime = null;
        lastFailureTime = null;

        for (String platform : successCounts.keySet()) {
            successCounts.get(platform).set(0);
            failureCounts.get(platform).set(0);
            consecutiveFailures.get(platform).set(0);
            lastExecutionTimes.remove(platform);
            lastErrorMessages.remove(platform);
        }
    }

    /**
     * Manual trigger for ETL processing (for testing/debugging)
     */
    public ParallelProcessingResult triggerManualExecution() {
        log.info("🔧 Manual ETL execution triggered");

        if (useParallelProcessing && parallelApiService != null) {
            return parallelApiService.processAllPlatformsInParallel();
        } else {
            return processAllPlatformsSequential();
        }
    }
}