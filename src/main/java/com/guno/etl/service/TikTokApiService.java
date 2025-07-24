// TikTokApiService.java - TikTok API Service
package com.guno.etl.service;

import com.guno.etl.dto.TikTokApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TikTokApiService {

    private static final Logger log = LoggerFactory.getLogger(TikTokApiService.class);

    private final WebClient webClient;
    private final String baseUrl;
    private final int pageSize;
    private final int maxRetries;
    private final Duration timeout;

    public TikTokApiService(
            @Value("${etl.api.tiktok.base-url}") String baseUrl,
            @Value("${etl.api.tiktok.auth-header:}") String authHeader,
            @Value("${etl.api.tiktok.api-key:}") String apiKey,
            @Value("${etl.api.tiktok.timeout:30}") Duration timeout,
            @Value("${etl.api.tiktok.page-size:20}") int pageSize,
            @Value("${etl.api.tiktok.max-retries:3}") int maxRetries) {

        this.baseUrl = baseUrl;
        this.pageSize = pageSize;
        this.maxRetries = maxRetries;
        this.timeout = timeout;

        // Build WebClient with TikTok-specific headers
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024));

        // Add default headers
        builder.defaultHeader("Content-Type", "application/json");
        builder.defaultHeader("Accept", "application/json");
        builder.defaultHeader("User-Agent", "Tiktok-ETL/1.0");

        // Add headers if provided
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            builder.defaultHeader(authHeader, apiKey);
            log.info("TikTok API Service initialized with auth header: ***CONFIGURED***");
        }


        this.webClient = builder.build();

        log.info("TikTok API Service initialized:");
        log.info("  Base URL: {}", baseUrl);
        log.info("  Timeout: {}s", timeout);
        log.info("  Page Size: {}", pageSize);
        log.info("  Max Retries: {}", maxRetries);
    }

    // ===== MAIN API METHODS FOR CURRENT DATE (SCHEDULER USAGE) =====

    /**
     * Fetch updated TikTok orders for current date (most common usage)
     */
    public TikTokApiResponse fetchUpdatedOrders() {
        return fetchUpdatedOrders(1);
    }

    /**
     * Fetch updated TikTok orders for current date with specific page
     */
    public TikTokApiResponse fetchUpdatedOrders(int page) {
        LocalDate currentDate = LocalDate.now();
        return fetchUpdatedOrdersForDate(currentDate, page, pageSize);
    }

    /**
     * Fetch updated TikTok orders for current date with page and limit
     */
    public TikTokApiResponse fetchUpdatedOrders(int page, int limit) {
        LocalDate currentDate = LocalDate.now();
        return fetchUpdatedOrdersForDate(currentDate, page, limit);
    }

    // ===== API METHODS FOR SPECIFIC DATE =====

    /**
     * Fetch updated TikTok orders for specific date (first page)
     */
    public TikTokApiResponse fetchUpdatedOrdersForDate(LocalDate date) {
        return fetchUpdatedOrdersForDate(date, 1, pageSize);
    }

    /**
     * Fetch updated TikTok orders for specific date and page
     */
    public TikTokApiResponse fetchUpdatedOrdersForDate(LocalDate date, int page) {
        return fetchUpdatedOrdersForDate(date, page, pageSize);
    }

    /**
     * Fetch updated TikTok orders for specific date, page, and limit
     */
    public TikTokApiResponse fetchUpdatedOrdersForDate(LocalDate date, int page, int limit) {
        return fetchOrdersWithFilter(date, page, limit, "update");
    }

    // ===== UTILITY API METHODS =====

    /**
     * Get total count of updated orders for current date
     */
    public int getTotalUpdatedOrderCount() {
        LocalDate currentDate = LocalDate.now();
        return getTotalUpdatedOrderCountForDate(currentDate);
    }

    /**
     * Get total count of updated orders for specific date
     */
    public int getTotalUpdatedOrderCountForDate(LocalDate date) {
        try {
            TikTokApiResponse response = fetchUpdatedOrdersForDate(date, 1, 1);
            return response != null && response.getData() != null ?
                    response.getData().getCount() : 0;
        } catch (Exception e) {
            log.error("Error getting total updated order count for date {}: {}", date, e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total pages for updated orders
     */
    public int calculateTotalPagesForUpdatedOrders() {
        int totalOrders = getTotalUpdatedOrderCount();
        return (int) Math.ceil((double) totalOrders / pageSize);
    }

    /**
     * Calculate total pages for specific date
     */
    public int calculateTotalPagesForDate(LocalDate date) {
        int totalOrders = getTotalUpdatedOrderCountForDate(date);
        return (int) Math.ceil((double) totalOrders / pageSize);
    }

    // ===== LEGACY METHODS (for backward compatibility) =====

    /**
     * Legacy method for fetching orders by date (similar to Shopee API)
     */
    public TikTokApiResponse fetchOrders(LocalDate date) {
        return fetchOrdersWithFilter(date, 1, pageSize, "update");
    }

    /**
     * Legacy method for fetching orders by date and page
     */
    public TikTokApiResponse fetchOrders(LocalDate date, int page) {
        return fetchOrdersWithFilter(date, page, pageSize, "update");
    }

    /**
     * Legacy method for fetching orders by date, page, and limit
     */
    public TikTokApiResponse fetchOrders(LocalDate date, int page, int limit) {
        return fetchOrdersWithFilter(date, page, limit, "update");
    }

    // ===== CORE API IMPLEMENTATION =====

    /**
     * Core method to fetch orders with specific filter
     */
    public TikTokApiResponse fetchOrdersWithFilter(LocalDate date, int page, int limit, String filterType) {
        try {
//            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dateStr = "2025-07-23";

            log.debug("Fetching TikTok orders for date: {} - page: {}, limit: {}, filter: {}",
                    dateStr, page, limit, filterType);

            TikTokApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("date", dateStr)
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam("filter-date", filterType)
                            .build())
                    .retrieve()
                    .bodyToMono(TikTokApiResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(500))
                            .filter(this::isRetryableException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("Max retries ({}) exceeded for TikTok API call", maxRetries);
                                return retrySignal.failure();
                            }))
                    .block();

            if (response != null) {
                log.debug("TikTok API response: Status={}, Message={}, Orders={}",
                        response.getStatus(), response.getMessage(),
                        response.getData() != null ? response.getData().getOrders().size() : 0);
            }

            return response;

        } catch (Exception e) {
            log.error("Error fetching TikTok orders for date {} (page {}, limit {}): {}",
                    date, page, limit, e.getMessage());
            throw new RuntimeException("TikTok API call failed", e);
        }
    }

    // ===== HEALTH CHECK AND UTILITY METHODS =====

    /**
     * Check if TikTok API is healthy
     */
    public boolean isApiHealthy() {
        try {
            TikTokApiResponse response = fetchUpdatedOrders(1, 1);
            return response != null && response.getStatus() == 1;
        } catch (Exception e) {
            log.warn("TikTok API health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test TikTok API connection
     */
    public String testConnection() {
        try {
            log.info("Testing TikTok API connection...");

            TikTokApiResponse response = fetchUpdatedOrders(1, 1);

            if (response != null) {
                if (response.getStatus() == 1) {
                    String result = "TikTok API connection successful";
                    log.info(result);
                    return result;
                } else {
                    String result = "TikTok API returned error: " + response.getMessage();
                    log.warn(result);
                    return result;
                }
            } else {
                String result = "TikTok API returned null response";
                log.error(result);
                return result;
            }

        } catch (Exception e) {
            String result = "TikTok API connection failed: " + e.getMessage();
            log.error(result, e);
            return result;
        }
    }

    // ===== MULTI-PAGE SUPPORT =====

    /**
     * Fetch all updated orders across multiple pages
     */
    public TikTokApiResponse fetchAllUpdatedOrders() {
        return fetchAllUpdatedOrdersForDate(LocalDate.now());
    }

    /**
     * Fetch all updated orders for specific date across multiple pages
     */
    public TikTokApiResponse fetchAllUpdatedOrdersForDate(LocalDate date) {
        try {
            // Get first page to determine total count
            TikTokApiResponse firstPage = fetchUpdatedOrdersForDate(date, 1);

            if (firstPage == null || firstPage.getData() == null) {
                return firstPage;
            }

            // If only one page, return first page
            int totalOrders = firstPage.getData().getCount();
            int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

            if (totalPages <= 1) {
                return firstPage;
            }

            // Fetch remaining pages and combine results
            for (int page = 2; page <= totalPages; page++) {
                TikTokApiResponse nextPage = fetchUpdatedOrdersForDate(date, page);
                if (nextPage != null && nextPage.getData() != null && nextPage.getData().getOrders() != null) {
                    firstPage.getData().getOrders().addAll(nextPage.getData().getOrders());
                }
            }

            log.info("Fetched {} TikTok orders across {} pages for date {}",
                    firstPage.getData().getOrders().size(), totalPages, date);

            return firstPage;

        } catch (Exception e) {
            log.error("Error fetching all TikTok orders for date {}: {}", date, e.getMessage());
            throw new RuntimeException("Failed to fetch all TikTok orders", e);
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Determine if exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) throwable;
            int statusCode = wcre.getStatusCode().value();

            // Retry on 5xx server errors and 429 rate limiting
            return statusCode >= 500 || statusCode == 429;
        }

        // Retry on connection timeouts and other network issues
        return throwable instanceof WebClientException;
    }

    // ===== GETTERS FOR CONFIGURATION =====

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getDefaultPageSize() {
        return pageSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getTimeout() {
        return timeout;
    }
}