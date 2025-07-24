// ShopeeApiService.java - Updated for date=update mode with Headers Support
package com.guno.etl.service;

import com.guno.etl.dto.ShopeeApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ShopeeApiService {

    private static final Logger log = LoggerFactory.getLogger(ShopeeApiService.class);

    private final WebClient webClient;
    private final String baseUrl;
    private final int defaultPageSize;
    private final int maxRetries;
    private final Duration timeout;

    public ShopeeApiService(
            @Value("${etl.api.shopee.base-url}") String baseUrl,
            @Value("${etl.api.shopee.timeout:60s}") Duration timeout,
            @Value("${etl.api.shopee.page-size:20}") int defaultPageSize,
            @Value("${etl.api.shopee.max-retries:3}") int maxRetries,
            @Value("${etl.api.shopee.auth-header}") String authHeader,
            @Value("${etl.api.shopee.api-key}") String apiKey) {

        this.baseUrl = baseUrl;
        this.defaultPageSize = defaultPageSize;
        this.maxRetries = maxRetries;
        this.timeout = timeout;

        // Build WebClient with headers
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)); // 10MB

        // Add default headers
        builder.defaultHeader("Content-Type", "application/json");
        builder.defaultHeader("Accept", "application/json");
        builder.defaultHeader("User-Agent", "Shopee-ETL/1.0");

        // Add authentication headers if provided
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            builder.defaultHeader(authHeader, apiKey);
            log.info("Added Authorization header to API client");
        }


        this.webClient = builder.build();

        log.info("ShopeeApiService initialized with base URL: {}", baseUrl);
        log.info("Default settings - Page size: {}, Timeout: {}, Max retries: {}",
                defaultPageSize, timeout, maxRetries);
        log.info("Headers configured - Auth: {}, API-Key: {}",
                authHeader != null && !authHeader.isEmpty() ? "***SET***" : "NOT_SET",
                apiKey != null && !apiKey.isEmpty() ? "***SET***" : "NOT_SET");
    }

    // ===== EXISTING METHODS (Date-based) =====

    /**
     * Fetch orders for specific date with pagination
     */
    public ShopeeApiResponse fetchOrders(LocalDate date, int page, int limit) {
//        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateStr = "2025-07-17";
        log.info("Fetching Shopee orders for date: {}, page: {}, limit: {}", dateStr, page, limit);

        try {
            ShopeeApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("date", dateStr)
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam("filter-date", "update")
                            .build())
                    .retrieve()
                    .bodyToMono(ShopeeApiResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(this::isRetryableException))
                    .block();

            if (response != null && response.getStatus() == 1) {
                log.info("Successfully fetched {} orders for date {}",
                        response.getData().getOrders().size(), dateStr);
                return response;
            } else {
                log.warn("API returned non-success status for date {}: {}", dateStr,
                        response != null ? response.getMessage() : "null response");
                return response;
            }

        } catch (Exception e) {
            log.error("Failed to fetch orders for date {} after {} retries: {}",
                    dateStr, maxRetries, e.getMessage());
            throw new RuntimeException("API call failed for date: " + dateStr, e);
        }
    }

    /**
     * Fetch orders for specific date with default pagination
     */
    public ShopeeApiResponse fetchOrders(LocalDate date, int page) {
        return fetchOrders(date, page, defaultPageSize);
    }

    /**
     * Fetch first page of orders for specific date
     */
    public ShopeeApiResponse fetchOrders(LocalDate date) {
        return fetchOrders(date, 1, defaultPageSize);
    }

    // ===== NEW METHODS (Updated orders for current date) =====

    /**
     * Fetch updated orders for current date with pagination
     * This is the main method for scheduled ETL process
     */
    public ShopeeApiResponse fetchUpdatedOrders(int page, int limit) {
        LocalDate currentDate = LocalDate.now();
//        String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateStr = "2025-07-17";

        log.info("Fetching updated Shopee orders for current date: {} - page: {}, limit: {}", dateStr, page, limit);

        try {
            ShopeeApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("date", dateStr)  // Current date in yyyy-MM-dd format
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam("filter-date", "update")
                            .build())
                    .retrieve()
                    .bodyToMono(ShopeeApiResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(this::isRetryableException))
                    .block();

            if (response != null && response.getStatus() == 1) {
                int orderCount = response.getData().getOrders().size();
                log.info("Successfully fetched {} updated orders for date {} on page {}", orderCount, dateStr, page);

                if (orderCount > 0) {
                    log.debug("Updated order IDs for {}: {}", dateStr,
                            response.getData().getOrders().stream()
                                    .map(order -> order.getOrderId())
                                    .toList());
                }

                return response;
            } else {
                log.warn("API returned non-success status for updated orders on date {}: {}", dateStr,
                        response != null ? response.getMessage() : "null response");
                return response;
            }

        } catch (Exception e) {
            log.error("Failed to fetch updated orders for date {} on page {} after {} retries: {}",
                    dateStr, page, maxRetries, e.getMessage());
            throw new RuntimeException("API call failed for updated orders on date " + dateStr + ", page: " + page, e);
        }
    }

    /**
     * Fetch updated orders for specific date with pagination
     */
    public ShopeeApiResponse fetchUpdatedOrdersForDate(LocalDate date, int page, int limit) {
//        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateStr = "2025-07-17";

        log.info("Fetching updated Shopee orders for date: {} - page: {}, limit: {}", dateStr, page, limit);

        try {
            ShopeeApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("date", dateStr)  // Specific date in yyyy-MM-dd format
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam("filter-date", "update")
                            .build())
                    .retrieve()
                    .bodyToMono(ShopeeApiResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(this::isRetryableException))
                    .block();

            if (response != null && response.getStatus() == 1) {
                int orderCount = response.getData().getOrders().size();
                log.info("Successfully fetched {} updated orders for date {} on page {}", orderCount, dateStr, page);

                return response;
            } else {
                log.warn("API returned non-success status for date {}: {}", dateStr,
                        response != null ? response.getMessage() : "null response");
                return response;
            }

        } catch (Exception e) {
            log.error("Failed to fetch updated orders for date {} on page {} after {} retries: {}",
                    dateStr, page, maxRetries, e.getMessage());
            throw new RuntimeException("API call failed for date " + dateStr + ", page: " + page, e);
        }
    }

    /**
     * Fetch updated orders with default pagination (current date)
     * Used by scheduler for simple calls
     */
    public ShopeeApiResponse fetchUpdatedOrders(int page) {
        return fetchUpdatedOrders(page, defaultPageSize);
    }

    /**
     * Fetch first page of updated orders (current date)
     * Most common use case for scheduler
     */
    public ShopeeApiResponse fetchUpdatedOrders() {
        return fetchUpdatedOrders(20, defaultPageSize);
    }

    /**
     * Fetch updated orders for specific date with default pagination
     */
    public ShopeeApiResponse fetchUpdatedOrdersForDate(LocalDate date, int page) {
        return fetchUpdatedOrdersForDate(date, page, defaultPageSize);
    }

    /**
     * Fetch updated orders for specific date (first page only)
     */
    public ShopeeApiResponse fetchUpdatedOrdersForDate(LocalDate date) {
        return fetchUpdatedOrdersForDate(date, 1, defaultPageSize);
    }

    /**
     * Fetch all updated orders across multiple pages (current date)
     * Use with caution - could be many pages
     */
    public ShopeeApiResponse fetchAllUpdatedOrders() {
        LocalDate currentDate = LocalDate.now();
        log.info("Fetching all updated orders for current date: {} across multiple pages", currentDate);

        ShopeeApiResponse firstPage = fetchUpdatedOrders(1);
        if (firstPage == null || firstPage.getStatus() != 1) {
            log.warn("Failed to fetch first page of updated orders for date: {}", currentDate);
            return firstPage;
        }

        int totalCount = firstPage.getData().getCount();
        int totalPages = calculateTotalPages(totalCount, defaultPageSize);

        log.info("Total updated orders for {}: {}, Total pages: {}", currentDate, totalCount, totalPages);

        if (totalPages <= 1) {
            return firstPage; // Only one page
        }

        // For now, return first page only
        // TODO: Implement multi-page aggregation if needed
        log.info("Returning first page only. Total pages available: {}", totalPages);
        return firstPage;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get total count of updated orders (current date)
     */
    public int getTotalUpdatedOrderCount() {
        LocalDate currentDate = LocalDate.now();
        try {
            ShopeeApiResponse response = fetchUpdatedOrders(1, 1); // Fetch only 1 item to get count
            if (response != null && response.getStatus() == 1) {
                int totalCount = response.getData().getCount();
                log.info("Total updated orders available for {}: {}", currentDate, totalCount);
                return totalCount;
            } else {
                log.warn("Failed to get updated order count for {}: {}", currentDate,
                        response != null ? response.getMessage() : "null response");
                return 0;
            }
        } catch (Exception e) {
            log.error("Error getting updated order count for {}: {}", currentDate, e.getMessage());
            return 0;
        }
    }

    /**
     * Get total count of updated orders for specific date
     */
    public int getTotalUpdatedOrderCountForDate(LocalDate date) {
        try {
            ShopeeApiResponse response = fetchUpdatedOrdersForDate(date, 1, 1); // Fetch only 1 item to get count
            if (response != null && response.getStatus() == 1) {
                int totalCount = response.getData().getCount();
                log.info("Total updated orders available for {}: {}", date, totalCount);
                return totalCount;
            } else {
                log.warn("Failed to get updated order count for {}: {}", date,
                        response != null ? response.getMessage() : "null response");
                return 0;
            }
        } catch (Exception e) {
            log.error("Error getting updated order count for {}: {}", date, e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total pages for updated orders (current date)
     */
    public int calculateTotalPagesForUpdatedOrders() {
        int totalCount = getTotalUpdatedOrderCount();
        return calculateTotalPages(totalCount, defaultPageSize);
    }

    /**
     * Calculate total pages for updated orders for specific date
     */
    public int calculateTotalPagesForUpdatedOrdersForDate(LocalDate date) {
        int totalCount = getTotalUpdatedOrderCountForDate(date);
        return calculateTotalPages(totalCount, defaultPageSize);
    }

    /**
     * Get total count of orders for specific date
     */
    public int getTotalOrderCount(LocalDate date) {
        try {
            ShopeeApiResponse response = fetchOrders(date, 1, 1); // Fetch only 1 item to get count
            if (response != null && response.getStatus() == 1) {
                int totalCount = response.getData().getCount();
                log.info("Total orders for date {}: {}", date, totalCount);
                return totalCount;
            } else {
                log.warn("Failed to get order count for date {}: {}", date,
                        response != null ? response.getMessage() : "null response");
                return 0;
            }
        } catch (Exception e) {
            log.error("Error getting order count for date {}: {}", date, e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total pages for specific date
     */
    public int calculateTotalPages(LocalDate date) {
        int totalCount = getTotalOrderCount(date);
        return calculateTotalPages(totalCount, defaultPageSize);
    }

    /**
     * Calculate total pages from count and page size
     */
    private int calculateTotalPages(int totalCount, int pageSize) {
        if (totalCount <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    // ===== HEALTH CHECK AND TEST METHODS =====

    /**
     * Test API connectivity (current date)
     */
    public String testConnection() {
        try {
            ShopeeApiResponse response = fetchUpdatedOrders(1, 1);
            if (response != null && response.getStatus() == 1) {
                return "✅ API connection successful - Status: " + response.getStatus() +
                        ", Message: " + response.getMessage() +
                        ", Date: " + LocalDate.now();
            } else {
                return "⚠️ API connection issues - Status: " +
                        (response != null ? response.getStatus() : "null") +
                        ", Message: " + (response != null ? response.getMessage() : "null response") +
                        ", Date: " + LocalDate.now();
            }
        } catch (Exception e) {
            return "❌ API connection failed - Error: " + e.getMessage() +
                    ", Date: " + LocalDate.now();
        }
    }

    /**
     * Check if API is healthy for updated orders (current date)
     */
    public boolean isApiHealthy() {
        try {
            ShopeeApiResponse response = fetchUpdatedOrders(1, 1);
            boolean healthy = response != null && response.getStatus() == 1;
            log.info("API health check for {} - Healthy: {}", LocalDate.now(), healthy);
            return healthy;
        } catch (Exception e) {
            log.warn("API health check failed for {}: {}", LocalDate.now(), e.getMessage());
            return false;
        }
    }

    /**
     * Test API with specific date (for backward compatibility)
     */
    public String testConnectionWithDate(LocalDate date) {
        try {
            ShopeeApiResponse response = fetchOrders(date, 1, 1);
            if (response != null && response.getStatus() == 1) {
                return "✅ API connection successful for date " + date +
                        " - Status: " + response.getStatus() +
                        ", Orders: " + response.getData().getOrders().size();
            } else {
                return "⚠️ API connection issues for date " + date +
                        " - Status: " + (response != null ? response.getStatus() : "null");
            }
        } catch (Exception e) {
            return "❌ API connection failed for date " + date + " - Error: " + e.getMessage();
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Determine if exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) throwable;
            int statusCode = wcre.getStatusCode().value();

            // Retry on server errors (5xx) and some client errors
            boolean retryable = statusCode >= 500 || statusCode == 429 || statusCode == 408;
            log.debug("HTTP {} - Retryable: {}", statusCode, retryable);
            return retryable;
        }

        if (throwable instanceof WebClientException) {
            // Retry on network issues, timeouts, etc.
            log.debug("WebClientException - Retryable: true - {}", throwable.getMessage());
            return true;
        }

        log.debug("Non-retryable exception: {} - {}",
                throwable.getClass().getSimpleName(), throwable.getMessage());
        return false;
    }

    // ===== GETTERS FOR CONFIGURATION =====

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getTimeout() {
        return timeout;
    }
}