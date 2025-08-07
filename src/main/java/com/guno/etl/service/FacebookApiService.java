// FacebookApiService.java - Facebook API Service (UPDATED FOR NEW CONFIG)
package com.guno.etl.service;

import com.guno.etl.dto.FacebookApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FacebookApiService {

    private static final Logger log = LoggerFactory.getLogger(FacebookApiService.class);

    private final WebClient webClient;

    // UPDATED: Using new configuration structure
    @Value("${etl.api.facebook.base-url}")
    private String baseUrl;

    @Value("${etl.api.facebook.auth-header}")
    private String authHeader;

    @Value("${etl.api.facebook.api-key}")
    private String apiKey;

    @Value("${etl.api.facebook.timeout:60s}")
    private String timeoutConfig;

    @Value("${etl.api.facebook.page-size:10}")
    private int defaultPageSize;

    @Value("${etl.api.facebook.max-retries:2}")
    private int retryAttempts;

    public FacebookApiService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ===== MAIN API METHODS =====

    /**
     * Fetch updated Facebook orders (default parameters)
     */
    public FacebookApiResponse fetchUpdatedOrders() {
        return fetchUpdatedOrders(1, defaultPageSize);
    }

    /**
     * Fetch updated Facebook orders with pagination
     */
    public FacebookApiResponse fetchUpdatedOrders(int page, int limit) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return fetchOrdersForDate(today, page, limit, "update");
    }

    /**
     * Fetch Facebook orders for specific date
     */
    public FacebookApiResponse fetchOrdersForDate(String date) {
        return fetchOrdersForDate(date, 1, defaultPageSize, "update");
    }

    /**
     * Fetch Facebook orders for specific date with pagination
     */
    public FacebookApiResponse fetchOrdersForDate(String date, int page, int limit) {
        return fetchOrdersForDate(date, page, limit, "update");
    }

    /**
     * Core method to fetch Facebook orders with all parameters
     */
    public FacebookApiResponse fetchOrdersForDate(String date, int page, int limit, String filterDate) {
        try {
            log.info("Fetching Facebook orders for date: {}, page: {}, limit: {}", date, page, limit);

            // Parse timeout from config (e.g., "60s" -> 60 seconds)
            Duration timeout = parseTimeout(timeoutConfig);

            FacebookApiResponse response = webClient.get()
                    .uri(baseUrl, uriBuilder -> uriBuilder
                            .queryParam("date", date)
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam("filter-date", filterDate)
                            .build())
                    .headers(this::addHeaders)
                    .retrieve()
                    .bodyToMono(FacebookApiResponse.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(2))
                            .filter(this::isRetryableException))
                    .timeout(timeout)
                    .block();

            if (response != null && response.getStatus() == 1) {
                int orderCount = response.getData() != null && response.getData().getOrders() != null ?
                        response.getData().getOrders().size() : 0;
                log.info("Successfully fetched {} Facebook orders", orderCount);
            } else {
                log.warn("Facebook API returned non-success status: {}",
                        response != null ? response.getStatus() : "null");
            }

            return response;

        } catch (Exception e) {
            log.error("Error fetching Facebook orders for date {} (page {}, limit {}): {}",
                    date, page, limit, e.getMessage());
            throw new RuntimeException("Facebook API call failed", e);
        }
    }

    // ===== UTILITY AND HEALTH CHECK METHODS =====

    /**
     * Check if Facebook API is healthy
     */
    public boolean isApiHealthy() {
        try {
            FacebookApiResponse response = fetchUpdatedOrders(1, 1);
            return response != null && response.getStatus() == 1;
        } catch (Exception e) {
            log.warn("Facebook API health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test Facebook API connection
     */
    public String testConnection() {
        try {
            log.info("Testing Facebook API connection...");

            FacebookApiResponse response = fetchUpdatedOrders(1, 1);

            if (response != null) {
                if (response.getStatus() == 1) {
                    String result = "Facebook API connection successful";
                    log.info(result);
                    return result;
                } else {
                    String result = "Facebook API returned error: " + response.getStatus();
                    log.warn(result);
                    return result;
                }
            } else {
                String result = "Facebook API returned null response";
                log.error(result);
                return result;
            }

        } catch (Exception e) {
            String result = "Facebook API connection failed: " + e.getMessage();
            log.error(result, e);
            return result;
        }
    }

    /**
     * Get total count of Facebook orders for today
     */
    public int getTotalOrderCount() {
        try {
            FacebookApiResponse response = fetchUpdatedOrders(1, 1);
            if (response != null && response.getData() != null) {
                return response.getData().getTotal() != null ? response.getData().getTotal() : 0;
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get Facebook order count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get total count of Facebook orders for specific date
     */
    public int getOrderCountForDate(String date) {
        try {
            FacebookApiResponse response = fetchOrdersForDate(date, 1, 1);
            if (response != null && response.getData() != null) {
                return response.getData().getTotal() != null ? response.getData().getTotal() : 0;
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get Facebook order count for date {}: {}", date, e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total pages for pagination
     */
    public int calculateTotalPages(int totalOrders, int pageSize) {
        if (totalOrders <= 0 || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalOrders / pageSize);
    }

    /**
     * Get Facebook API configuration info
     */
    public String getApiInfo() {
        return String.format("Facebook API - URL: %s, Timeout: %s, Retry: %d, PageSize: %d",
                baseUrl, timeoutConfig, retryAttempts, defaultPageSize);
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Add required headers to Facebook API requests
     */
    private void addHeaders(HttpHeaders headers) {
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set(authHeader, apiKey);  // Use auth-header as header name
        }
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Parse timeout configuration (e.g., "60s" -> Duration.ofSeconds(60))
     */
    private Duration parseTimeout(String timeoutStr) {
        try {
            if (timeoutStr.endsWith("s")) {
                int seconds = Integer.parseInt(timeoutStr.substring(0, timeoutStr.length() - 1));
                return Duration.ofSeconds(seconds);
            } else if (timeoutStr.endsWith("m")) {
                int minutes = Integer.parseInt(timeoutStr.substring(0, timeoutStr.length() - 1));
                return Duration.ofMinutes(minutes);
            } else {
                // Assume seconds if no unit
                return Duration.ofSeconds(Integer.parseInt(timeoutStr));
            }
        } catch (Exception e) {
            log.warn("Failed to parse timeout '{}', using default 60s", timeoutStr);
            return Duration.ofSeconds(60);
        }
    }

    /**
     * Determine if exception is retryable
     */
    private boolean isRetryableException(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webClientEx = (WebClientResponseException) ex;
            int statusCode = webClientEx.getStatusCode().value();
            // Retry for 5xx server errors and 429 rate limiting
            return statusCode >= 500 || statusCode == 429;
        }
        // Retry for timeout and connection issues
        return ex instanceof java.net.SocketTimeoutException ||
                ex instanceof java.net.ConnectException ||
                ex instanceof java.util.concurrent.TimeoutException;
    }
}