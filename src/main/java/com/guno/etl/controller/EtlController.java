// EtlController.java - Compatible Multi-Platform REST API Controller
package com.guno.etl.controller;

import com.guno.etl.service.ShopeeEtlService;
import com.guno.etl.service.TikTokEtlService;
import com.guno.etl.service.ShopeeApiService;
import com.guno.etl.service.TikTokApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/etl")
public class EtlController {

    private static final Logger log = LoggerFactory.getLogger(EtlController.class);

    @Autowired
    private ShopeeEtlService shopeeEtlService;

    @Autowired
    private TikTokEtlService tiktokEtlService;

    @Autowired
    private ShopeeApiService shopeeApiService;

    @Autowired
    private TikTokApiService tiktokApiService;

    @Value("${etl.platforms.shopee.enabled:true}")
    private boolean shopeeEnabled;

    @Value("${etl.platforms.tiktok.enabled:true}")
    private boolean tiktokEnabled;

    // ===== MULTI-PLATFORM ETL OPERATIONS =====

    /**
     * Trigger ETL for all enabled platforms
     */
    @PostMapping("/trigger-all")
    public ResponseEntity<Map<String, Object>> triggerAllPlatforms() {
        log.info("Manual trigger requested for all platforms");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();

        try {
            // Trigger Shopee if enabled
            if (shopeeEnabled) {
                try {
                    ShopeeEtlService.EtlResult shopeeResult = shopeeEtlService.processUpdatedOrders();
                    results.put("shopee", createPlatformResult("SHOPEE", shopeeResult.isSuccess(),
                            shopeeResult.getOrdersProcessed(), shopeeResult.getTotalOrders(),
                            shopeeResult.getDurationMs(), shopeeResult.getErrorMessage()));
                } catch (Exception e) {
                    log.error("Shopee ETL failed: {}", e.getMessage());
                    results.put("shopee", createPlatformResult("SHOPEE", false, 0, 0, 0L, e.getMessage()));
                }
            } else {
                results.put("shopee", createPlatformResult("SHOPEE", false, 0, 0, 0L, "Platform disabled"));
            }

            // Trigger TikTok if enabled
            if (tiktokEnabled) {
                try {
                    TikTokEtlService.EtlResult tiktokResult = tiktokEtlService.processUpdatedOrders();
                    results.put("tiktok", createPlatformResult("TIKTOK", tiktokResult.isSuccess(),
                            tiktokResult.getOrdersProcessed(), tiktokResult.getTotalOrders(),
                            tiktokResult.getDurationMs(), tiktokResult.getErrorMessage()));
                } catch (Exception e) {
                    log.error("TikTok ETL failed: {}", e.getMessage());
                    results.put("tiktok", createPlatformResult("TIKTOK", false, 0, 0, 0L, e.getMessage()));
                }
            } else {
                results.put("tiktok", createPlatformResult("TIKTOK", false, 0, 0, 0L, "Platform disabled"));
            }

            response.put("success", true);
            response.put("message", "Multi-platform ETL triggered successfully");
            response.put("platforms", results);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Multi-platform ETL trigger failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Multi-platform ETL trigger failed: " + e.getMessage());
            response.put("platforms", results);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===== SHOPEE-SPECIFIC OPERATIONS =====

    /**
     * Trigger Shopee ETL manually
     */
    @PostMapping("/shopee/trigger")
    public ResponseEntity<Map<String, Object>> triggerShopeeEtl() {
        log.info("Manual Shopee ETL trigger requested");

        if (!shopeeEnabled) {
            return createErrorResponse("Shopee platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            ShopeeEtlService.EtlResult result = shopeeEtlService.processUpdatedOrders();

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("platform", "SHOPEE");
            response.put("ordersProcessed", result.getOrdersProcessed());
            response.put("totalOrders", result.getTotalOrders());
            response.put("successRate", calculateSuccessRate(result.getOrdersProcessed(), result.getTotalOrders()) + "%");
            response.put("duration", result.getDurationMs() + "ms");
            response.put("errorMessage", result.getErrorMessage());
            response.put("failedOrders", result.getFailedOrders().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Shopee ETL failed: {}", e.getMessage());
            return createErrorResponse("Shopee ETL failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Test Shopee API connectivity
     */
    @GetMapping("/shopee/api-test")
    public ResponseEntity<Map<String, Object>> testShopeeApi() {
        if (!shopeeEnabled) {
            return createErrorResponse("Shopee platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            String result = shopeeApiService.testConnection();
            boolean isHealthy = !result.contains("failed") && !result.contains("error");

            Map<String, Object> response = new HashMap<>();
            response.put("success", isHealthy);
            response.put("platform", "SHOPEE");
            response.put("connectionTest", result);
            response.put("apiHealthy", isHealthy);
            response.put("message", isHealthy ? "Shopee API is accessible" : "Shopee API has issues");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return createErrorResponse("Shopee API test failed: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Get Shopee order count for today
     */
    @GetMapping("/shopee/order-count")
    public ResponseEntity<Map<String, Object>> getShopeeOrderCount() {
        if (!shopeeEnabled) {
            return createErrorResponse("Shopee platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            LocalDate today = LocalDate.now();
            long orderCount = shopeeApiService.getTotalOrderCount(today);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("platform", "SHOPEE");
            response.put("orderCount", orderCount);
            response.put("date", today.toString());
            response.put("message", "Retrieved order count for today");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get Shopee order count: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== TIKTOK-SPECIFIC OPERATIONS =====

    /**
     * Trigger TikTok ETL manually
     */
    @PostMapping("/tiktok/trigger")
    public ResponseEntity<Map<String, Object>> triggerTikTokEtl() {
        log.info("Manual TikTok ETL trigger requested");

        if (!tiktokEnabled) {
            return createErrorResponse("TikTok platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            TikTokEtlService.EtlResult result = tiktokEtlService.processUpdatedOrders();

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("platform", "TIKTOK");
            response.put("ordersProcessed", result.getOrdersProcessed());
            response.put("totalOrders", result.getTotalOrders());
            response.put("successRate", calculateSuccessRate(result.getOrdersProcessed(), result.getTotalOrders()) + "%");
            response.put("duration", result.getDurationMs() + "ms");
            response.put("errorMessage", result.getErrorMessage());
            response.put("failedOrders", result.getFailedOrders().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("TikTok ETL failed: {}", e.getMessage());
            return createErrorResponse("TikTok ETL failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Test TikTok API connectivity
     */
    @GetMapping("/tiktok/api-test")
    public ResponseEntity<Map<String, Object>> testTikTokApi() {
        if (!tiktokEnabled) {
            return createErrorResponse("TikTok platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            String result = tiktokApiService.testConnection();
            boolean isHealthy = !result.contains("failed") && !result.contains("error");

            Map<String, Object> response = new HashMap<>();
            response.put("success", isHealthy);
            response.put("platform", "TIKTOK");
            response.put("connectionTest", result);
            response.put("apiHealthy", isHealthy);
            response.put("message", isHealthy ? "TikTok API is accessible" : "TikTok API has issues");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return createErrorResponse("TikTok API test failed: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Get TikTok order count for today
     */
    @GetMapping("/tiktok/order-count")
    public ResponseEntity<Map<String, Object>> getTikTokOrderCount() {
        if (!tiktokEnabled) {
            return createErrorResponse("TikTok platform is disabled", HttpStatus.BAD_REQUEST);
        }

        try {
            LocalDate today = LocalDate.now();
            long orderCount = tiktokApiService.getTotalOrderCount(today);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("platform", "TIKTOK");
            response.put("orderCount", orderCount);
            response.put("date", today.toString());
            response.put("message", "Retrieved order count for today");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get TikTok order count: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== SYSTEM STATUS =====

    /**
     * Get overall system status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> platformStatus = new HashMap<>();

        try {
            // Shopee status
            if (shopeeEnabled) {
                try {
                    String testResult = shopeeApiService.testConnection();
                    boolean shopeeHealthy = !testResult.contains("failed") && !testResult.contains("error");
                    platformStatus.put("shopee", Map.of(
                            "enabled", true,
                            "apiHealthy", shopeeHealthy,
                            "status", shopeeHealthy ? "HEALTHY" : "UNHEALTHY"
                    ));
                } catch (Exception e) {
                    platformStatus.put("shopee", Map.of(
                            "enabled", true,
                            "apiHealthy", false,
                            "status", "ERROR",
                            "error", e.getMessage()
                    ));
                }
            } else {
                platformStatus.put("shopee", Map.of(
                        "enabled", false,
                        "status", "DISABLED"
                ));
            }

            // TikTok status
            if (tiktokEnabled) {
                try {
                    String testResult = tiktokApiService.testConnection();
                    boolean tiktokHealthy = !testResult.contains("failed") && !testResult.contains("error");
                    platformStatus.put("tiktok", Map.of(
                            "enabled", true,
                            "apiHealthy", tiktokHealthy,
                            "status", tiktokHealthy ? "HEALTHY" : "UNHEALTHY"
                    ));
                } catch (Exception e) {
                    platformStatus.put("tiktok", Map.of(
                            "enabled", true,
                            "apiHealthy", false,
                            "status", "ERROR",
                            "error", e.getMessage()
                    ));
                }
            } else {
                platformStatus.put("tiktok", Map.of(
                        "enabled", false,
                        "status", "DISABLED"
                ));
            }

            response.put("success", true);
            response.put("systemStatus", "OPERATIONAL");
            response.put("platforms", platformStatus);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("systemStatus", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get API help documentation
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getApiHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("multiPlatformOperations", Map.of(
                "POST /api/etl/trigger-all", "Trigger ETL for all enabled platforms",
                "GET /api/etl/status", "Get overall system status"
        ));

        help.put("shopeeOperations", Map.of(
                "POST /api/etl/shopee/trigger", "Trigger Shopee ETL manually",
                "GET /api/etl/shopee/api-test", "Test Shopee API connectivity",
                "GET /api/etl/shopee/order-count", "Get Shopee order count for today"
        ));

        help.put("tiktokOperations", Map.of(
                "POST /api/etl/tiktok/trigger", "Trigger TikTok ETL manually",
                "GET /api/etl/tiktok/api-test", "Test TikTok API connectivity",
                "GET /api/etl/tiktok/order-count", "Get TikTok order count for today"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Multi-Platform ETL API Help");
        response.put("endpoints", help);
        response.put("note", "All endpoints support both Shopee and TikTok platforms");

        return ResponseEntity.ok(response);
    }

    // ===== UTILITY METHODS =====

    private Map<String, Object> createPlatformResult(String platform, boolean success,
                                                     int processed, int total, long duration, String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("platform", platform);
        result.put("success", success);
        result.put("ordersProcessed", processed);
        result.put("totalOrders", total);
        result.put("successRate", calculateSuccessRate(processed, total));
        result.put("duration", duration + "ms");
        if (error != null) {
            result.put("error", error);
        }
        return result;
    }

    private double calculateSuccessRate(int processed, int total) {
        return total > 0 ? (double) processed / total * 100 : 0;
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(response);
    }
}