package com.guno.etl.service;

import com.guno.etl.dto.ShopeeApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ShopeeApiService {

    private static final Logger log = LoggerFactory.getLogger(ShopeeApiService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String authHeader;
    private final String apiKey;
    private final String contentType;
    private final int timeout;
    private final int pageSize;
    private final int maxRetries;

    public ShopeeApiService(
            RestTemplate restTemplate,
            @Value("${etl.api.shopee.base-url}") String baseUrl,
            @Value("${etl.api.shopee.auth-header:}") String authHeader,
            @Value("${etl.api.shopee.api-key:}") String apiKey,
            @Value("${etl.api.shopee.content-type:application/json}") String contentType,
            @Value("${etl.api.shopee.timeout:60}") int timeout,
            @Value("${etl.api.shopee.page-size:50}") int pageSize,
            @Value("${etl.api.shopee.max-retries:3}") int maxRetries) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.authHeader = authHeader;
        this.apiKey = apiKey;
        this.contentType = contentType;
        this.timeout = timeout;
        this.pageSize = pageSize;
        this.maxRetries = maxRetries;

        log.info("ShopeeApiService initialized with base URL: {}", baseUrl);
    }

    /**
     * Fetch Shopee orders for updated data (default: today)
     * @return API response with orders
     */
    public ShopeeApiResponse fetchUpdatedOrders() {
        return fetchOrdersForDate(LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Fetch Shopee orders for a specific date
     * @param dateString Date in YYYY-MM-DD format
     * @return API response with orders
     */
    public ShopeeApiResponse fetchOrdersForDate(String dateString) {
        return fetchOrders(dateString, 1, pageSize);
    }

    /**
     * Fetch Shopee orders with pagination
     * @param dateString Date in YYYY-MM-DD format
     * @param page Page number (1-based)
     * @param limit Orders per page
     * @return API response with orders
     */
    public ShopeeApiResponse fetchOrders(String dateString, int page, int limit) {
        try {
            log.info("Fetching Shopee orders for date: {}, page: {}, limit: {}", dateString, page, limit);

            // Build URL with parameters
            String url = String.format("%s?date=%s&page=%d&limit=%d&filter-date=update",
                    baseUrl, dateString, page, limit);


            // Create request with headers
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make API call with retry logic
            ResponseEntity<ShopeeApiResponse> response = makeApiCallWithRetry(url, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ShopeeApiResponse apiResponse = response.getBody();
                int orderCount = (apiResponse.getData() != null && apiResponse.getData().getOrders() != null)
                        ? apiResponse.getData().getOrders().size() : 0;

                log.info("Successfully fetched {} Shopee orders for date: {}", orderCount, dateString);
                return apiResponse;
            } else {
                log.warn("Shopee API returned no data for date: {}", dateString);
                return null;
            }

        } catch (Exception e) {
            log.error("Failed to fetch Shopee orders for date {}: {}", dateString, e.getMessage());
            throw new RuntimeException("Shopee API call failed for date: " + dateString, e);
        }
    }

    /**
     * Test Shopee API connection
     * @return Connection test result
     */
    public String testConnection() {
        try {
            log.info("Testing Shopee API connection...");

            String testDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            String url = String.format("%s?date=%s&page=1&limit=1&filter-date=update", baseUrl, testDate);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ShopeeApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ShopeeApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Shopee API connection test successful");
                return "Shopee API connection successful";
            } else {
                log.warn("Shopee API connection test failed with status: {}", response.getStatusCode());
                return "Shopee API connection failed: " + response.getStatusCode();
            }

        } catch (Exception e) {
            log.error("Shopee API connection test failed: {}", e.getMessage());
            return "Shopee API connection failed: " + e.getMessage();
        }
    }

    /**
     * Check if Shopee API is healthy
     * @return true if API is accessible
     */
    public boolean isApiHealthy() {
        try {
            String result = testConnection();
            return result.contains("successful");
        } catch (Exception e) {
            log.error("Shopee API health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get total order count for a specific date
     * @param date Target date
     * @return Total number of orders
     */
    public long getTotalOrderCount(LocalDate date) {
        try {
            String dateString = date.format(DATE_FORMATTER);
            ShopeeApiResponse response = fetchOrders(dateString, 1, 1);

            if (response != null && response.getData() != null) {
                // Assuming API returns total count in response metadata
                // This might need adjustment based on actual Shopee API response structure
                return response.getData().getCount() != null ? response.getData().getCount() : 0;
            }

            return 0;
        } catch (Exception e) {
            log.error("Failed to get Shopee order count for date {}: {}", date, e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total pages for pagination
     * @param date Target date
     * @return Total number of pages
     */
    public int calculateTotalPages(LocalDate date) {
        try {
            long totalOrders = getTotalOrderCount(date);
            return (int) Math.ceil((double) totalOrders / pageSize);
        } catch (Exception e) {
            log.error("Failed to calculate total pages for date {}: {}", date, e.getMessage());
            return 1;
        }
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authHeader != null && !authHeader.isEmpty() && apiKey != null && !apiKey.isEmpty()) {
            headers.set(authHeader, apiKey);
        }

        return headers;
    }

    private ResponseEntity<ShopeeApiResponse> makeApiCallWithRetry(String url, HttpEntity<String> entity) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                attempts++;
                log.debug("Shopee API call attempt {} of {}", attempts, maxRetries);

                ResponseEntity<ShopeeApiResponse> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, ShopeeApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return response;
                }

                log.warn("Shopee API returned non-success status: {} on attempt {}",
                        response.getStatusCode(), attempts);

            } catch (HttpClientErrorException e) {
                lastException = e;
                if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500) {
                    // Client errors (4xx) - don't retry
                    log.error("Shopee API client error (4xx): {}", e.getMessage());
                    break;
                }
                log.warn("Shopee API server error on attempt {}: {}", attempts, e.getMessage());

            } catch (ResourceAccessException e) {
                lastException = e;
                log.warn("Shopee API timeout/connection error on attempt {}: {}", attempts, e.getMessage());

            } catch (Exception e) {
                lastException = e;
                log.warn("Shopee API unexpected error on attempt {}: {}", attempts, e.getMessage());
            }

            // Wait before retry (except for last attempt)
            if (attempts < maxRetries) {
                try {
                    Thread.sleep(1000L * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries failed
        String errorMessage = String.format("Shopee API failed after %d attempts", maxRetries);
        if (lastException != null) {
            errorMessage += ": " + lastException.getMessage();
        }

        log.error(errorMessage);
        throw new RuntimeException(errorMessage, lastException);
    }

    // Getter methods for configuration inspection
    public String getBaseUrl() { return baseUrl; }
    public int getPageSize() { return pageSize; }
    public int getTimeout() { return timeout; }
    public int getMaxRetries() { return maxRetries; }
}