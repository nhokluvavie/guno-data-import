package com.guno.etl.service;

import com.guno.etl.dto.EtlResult;
import com.guno.etl.dto.ParallelProcessingResult;
import com.guno.etl.dto.FailedOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ParallelApiService {

    @Autowired
    private ShopeeEtlService shopeeEtlService;

    @Autowired
    private TikTokEtlService tiktokEtlService;

    @Autowired
    private FacebookEtlService facebookEtlService;

    @Value("${etl.parallel.enabled:true}")
    private boolean parallelEnabled;

    @Value("${etl.parallel.timeout-minutes:15}")
    private int timeoutMinutes;

    @Value("${etl.parallel.thread-pool-size:5}")
    private int threadPoolSize;

    @Value("${etl.platforms.shopee.enabled:true}")
    private boolean shopeeEnabled;

    @Value("${etl.platforms.tiktok.enabled:true}")
    private boolean tiktokEnabled;

    @Value("${etl.platforms.facebook.enabled:true}")
    private boolean facebookEnabled;

    private final ExecutorService executorService;

    public ParallelApiService() {
        this.executorService = Executors.newFixedThreadPool(3); // 3 platforms
    }

    /**
     * Process all enabled platforms in parallel
     * @return Combined results from all platforms
     */
    public ParallelProcessingResult processAllPlatformsInParallel() {
        log.info("🚀 Starting PARALLEL ETL processing for all platforms");

        ParallelProcessingResult result = new ParallelProcessingResult();
        result.setStartTime(LocalDateTime.now());
        result.setParallelEnabled(parallelEnabled);

        try {
            if (!parallelEnabled) {
                log.info("⚠️  Parallel processing DISABLED - falling back to sequential");
                return processSequentially();
            }

            // Create CompletableFutures for each enabled platform
            CompletableFuture<EtlResult> shopeeTask = createShopeeTask();
            CompletableFuture<EtlResult> tiktokTask = createTiktokTask();
            CompletableFuture<EtlResult> facebookTask = createFacebookTask();

            // Wait for all tasks to complete with timeout
            log.info("⏱️  Waiting for all platforms to complete (timeout: {} minutes)", timeoutMinutes);

            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                    shopeeTask, tiktokTask, facebookTask
            );

            // Apply global timeout
            allTasks.get(timeoutMinutes, TimeUnit.MINUTES);

            // Collect results
            result.setShopeeResult(shopeeTask.get());
            result.setTiktokResult(tiktokTask.get());
            result.setFacebookResult(facebookTask.get());

            // Calculate summary
            calculateSummary(result);

            result.setSuccess(true);
            result.setEndTime(LocalDateTime.now());

            log.info("✅ PARALLEL ETL processing completed successfully");
            logSummary(result);

            return result;

        } catch (Exception e) {
            log.error("❌ PARALLEL ETL processing failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    /**
     * Create Shopee processing task
     */
    private CompletableFuture<EtlResult> createShopeeTask() {
        if (!shopeeEnabled) {
            log.info("📱 Shopee ETL DISABLED - skipping");
            return CompletableFuture.completedFuture(createSkippedResult("SHOPEE"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("📱 Starting Shopee ETL in parallel thread: {}", Thread.currentThread().getName());
                EtlResult result = shopeeEtlService.processUpdatedOrders();
                log.info("📱 Shopee ETL completed: {} orders processed", result.getOrdersProcessed());
                return result;
            } catch (Exception e) {
                log.error("📱 Shopee ETL failed: {}", e.getMessage(), e);
                return createErrorResult("SHOPEE", e.getMessage());
            }
        }, executorService);
    }

    /**
     * Create TikTok processing task
     */
    private CompletableFuture<EtlResult> createTiktokTask() {
        if (!tiktokEnabled) {
            log.info("🎵 TikTok ETL DISABLED - skipping");
            return CompletableFuture.completedFuture(createSkippedResult("TIKTOK"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("🎵 Starting TikTok ETL in parallel thread: {}", Thread.currentThread().getName());
                EtlResult result = tiktokEtlService.processUpdatedOrders();
                log.info("🎵 TikTok ETL completed: {} orders processed", result.getOrdersProcessed());
                return result;
            } catch (Exception e) {
                log.error("🎵 TikTok ETL failed: {}", e.getMessage(), e);
                return createErrorResult("TIKTOK", e.getMessage());
            }
        }, executorService);
    }

    /**
     * Create Facebook processing task with special handling
     */
    private CompletableFuture<EtlResult> createFacebookTask() {
        if (!facebookEnabled) {
            log.info("📘 Facebook ETL DISABLED - skipping");
            return CompletableFuture.completedFuture(createSkippedResult("FACEBOOK"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("📘 Starting Facebook ETL in parallel thread: {}", Thread.currentThread().getName());
                log.warn("📘 Facebook ETL may take longer due to API limitations");

                // Facebook only has processOrdersForDate, so use today's date
                String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                EtlResult result = facebookEtlService.processOrdersForDate(todayDate);
                log.info("📘 Facebook ETL completed: {} orders processed", result.getOrdersProcessed());
                return result;
            } catch (Exception e) {
                log.error("📘 Facebook ETL failed: {}", e.getMessage(), e);
                return createErrorResult("FACEBOOK", e.getMessage());
            }
        }, executorService);
    }

    /**
     * Fallback to sequential processing if parallel is disabled
     */
    private ParallelProcessingResult processSequentially() {
        log.info("🔄 Processing platforms SEQUENTIALLY");

        ParallelProcessingResult result = new ParallelProcessingResult();
        result.setStartTime(LocalDateTime.now());
        result.setParallelEnabled(false);

        try {
            // Process each platform sequentially
            if (shopeeEnabled && shopeeEtlService != null) {
                log.info("📱 Processing Shopee ETL...");
                result.setShopeeResult(shopeeEtlService.processUpdatedOrders());
            }

            if (tiktokEnabled && tiktokEtlService != null) {
                log.info("🎵 Processing TikTok ETL...");
                result.setTiktokResult(tiktokEtlService.processUpdatedOrders());
            }

            if (facebookEnabled && facebookEtlService != null) {
                log.info("📘 Processing Facebook ETL...");
                String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                result.setFacebookResult(facebookEtlService.processOrdersForDate(todayDate));
            }

            calculateSummary(result);
            result.setSuccess(true);
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

    /**
     * Calculate summary statistics
     */
    private void calculateSummary(ParallelProcessingResult result) {
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
     * Log processing summary
     */
    private void logSummary(ParallelProcessingResult result) {
        long durationMinutes = java.time.Duration.between(
                result.getStartTime(), result.getEndTime()
        ).toMinutes();

        log.info("📊 ETL SUMMARY:");
        log.info("   ⏱️  Duration: {} minutes", durationMinutes);
        log.info("   📊 Total Orders: {}", result.getTotalOrders());
        log.info("   ✅ Processed: {}", result.getTotalProcessed());
        log.info("   ❌ Failed: {}", result.getTotalFailed());
        log.info("   🎯 Success Rate: {:.1f}%",
                result.getTotalOrders() > 0 ?
                        (double) result.getTotalProcessed() / result.getTotalOrders() * 100 : 0);

        if (result.getShopeeResult() != null) {
            log.info("   📱 Shopee: {}/{} orders",
                    result.getShopeeResult().getOrdersProcessed(),
                    result.getShopeeResult().getTotalOrders());
        }

        if (result.getTiktokResult() != null) {
            log.info("   🎵 TikTok: {}/{} orders",
                    result.getTiktokResult().getOrdersProcessed(),
                    result.getTiktokResult().getTotalOrders());
        }

        if (result.getFacebookResult() != null) {
            log.info("   📘 Facebook: {}/{} orders",
                    result.getFacebookResult().getOrdersProcessed(),
                    result.getFacebookResult().getTotalOrders());
        }
    }

    /**
     * Create result for skipped platform
     */
    private EtlResult createSkippedResult(String platform) {
        EtlResult result = new EtlResult();
        result.setSuccess(true);
        result.setTotalOrders(0);
        result.setOrdersProcessed(0);
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now());
        result.setErrorMessage("Platform " + platform + " is disabled");
        return result;
    }

    /**
     * Create result for failed platform
     */
    private EtlResult createErrorResult(String platform, String errorMessage) {
        EtlResult result = new EtlResult();
        result.setSuccess(false);
        result.setTotalOrders(0);
        result.setOrdersProcessed(0);
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now());
        result.setErrorMessage(platform + " ETL failed: " + errorMessage);
        return result;
    }

    /**
     * Shutdown executor service gracefully
     */
    public void shutdown() {
        log.info("🔄 Shutting down ParallelApiService executor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}