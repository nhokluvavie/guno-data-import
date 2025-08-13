package com.guno.etl.service;

import com.guno.etl.dto.FacebookApiResponse;
import com.guno.etl.dto.FacebookOrderDto;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class FacebookApiService {

    private static final Logger log = LoggerFactory.getLogger(FacebookApiService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String authHeader;
    private final String apiKey;
    private final String contentType;
    private final int timeout;
    private final int pageSize;
    private final int maxRetries;

    public FacebookApiService(
            RestTemplate restTemplate,
            @Value("${etl.api.facebook.base-url}") String baseUrl,
            @Value("${etl.api.facebook.auth-header:}") String authHeader,
            @Value("${etl.api.facebook.api-key:}") String apiKey,
            @Value("${etl.api.facebook.content-type:application/json}") String contentType,
            @Value("${etl.api.facebook.timeout:60}") int timeout,
            @Value("${etl.api.facebook.page-size:50}") int pageSize,
            @Value("${etl.api.facebook.max-retries:3}") int maxRetries) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.authHeader = authHeader;
        this.apiKey = apiKey;
        this.contentType = contentType;
        this.timeout = timeout;
        this.pageSize = pageSize;
        this.maxRetries = maxRetries;

        log.info("FacebookApiService initialized with base URL: {}", baseUrl);
    }

    /**
     * Fetch Facebook orders for updated data (default: today)
     * @return API response with orders
     */
    public FacebookApiResponse fetchUpdatedOrders() {
        return fetchOrdersForDate(LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Fetch Facebook orders for a specific date
     * @param dateString Date in YYYY-MM-DD format
     * @return API response with orders
     */
    public FacebookApiResponse fetchOrdersForDate(String dateString) {
        return fetchOrders(dateString, 1, pageSize);
    }

    /**
     * Fetch Facebook orders with pagination
     * @param dateString Date in YYYY-MM-DD format
     * @param page Page number (1-based)
     * @param limit Orders per page
     * @return API response with orders
     */
    public FacebookApiResponse fetchOrders(String dateString, int page, int limit) {
        try {
            log.info("Fetching Facebook orders for date: {}, page: {}, limit: {}", dateString, page, limit);

            // Build URL with parameters
            String url = String.format("%s?date=%s&page=%d&limit=%d&filter-date=update",
                    baseUrl, dateString, page, limit);

            // Create request with headers
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make API call with retry logic
            ResponseEntity<FacebookApiResponse> response = makeApiCallWithRetry(url, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FacebookApiResponse apiResponse = response.getBody();
                int orderCount = (apiResponse.getData() != null && apiResponse.getData().getOrders() != null)
                        ? apiResponse.getData().getOrders().size() : 0;

                log.info("Successfully fetched {} Facebook orders for date: {}", orderCount, dateString);
                return apiResponse;
            } else {
                log.warn("Facebook API returned no data for date: {}", dateString);
                return null;
            }

        } catch (Exception e) {
            log.error("Failed to fetch Facebook orders for date {}: {}", dateString, e.getMessage());
            throw new RuntimeException("Facebook API call failed for date: " + dateString, e);
        }
    }

    /**
     * Fetch ALL Facebook orders for a specific date (with pagination)
     * @param dateString Date in YYYY-MM-DD format
     * @return Combined API response with all orders
     */
    public FacebookApiResponse fetchAllOrdersForDate(String dateString) {
        try {
            log.info("Fetching ALL Facebook orders for date: {}", dateString);

            List<FacebookOrderDto> allOrders = new ArrayList<>();
            int currentPage = 1;
            int totalCount = 0;

            do {
                FacebookApiResponse response = fetchOrders(dateString, currentPage, pageSize);

                if (response != null && response.getData() != null && response.getData().getOrders() != null) {
                    allOrders.addAll(response.getData().getOrders());
                    totalCount = response.getData().getCount(); // Get total count

                    log.info("Fetched page {}: {} orders, total: {}",
                            currentPage, response.getData().getOrders().size(), totalCount);

                    currentPage++;
                } else {
                    break;
                }

            } while (allOrders.size() < totalCount);

            // Create combined response
            FacebookApiResponse combinedResponse = new FacebookApiResponse();
            FacebookApiResponse.FacebookDataWrapper dataWrapper = new FacebookApiResponse.FacebookDataWrapper();
            dataWrapper.setOrders(allOrders);
            dataWrapper.setCount(totalCount);
            dataWrapper.setPage(1);
            combinedResponse.setData(dataWrapper);

            log.info("Completed fetching ALL {} Facebook orders for date: {}", allOrders.size(), dateString);
            return combinedResponse;

        } catch (Exception e) {
            log.error("Failed to fetch all Facebook orders for date {}: {}", dateString, e.getMessage());
            throw new RuntimeException("Failed to fetch all orders", e);
        }
    }

    /**
     * Test Facebook API connection
     * @return Connection test result
     */
    public String testConnection() {
        try {
            log.info("Testing Facebook API connection...");

            String testDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            String url = String.format("%s?date=%s&page=1&limit=1&filter-date=update", baseUrl, testDate);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<FacebookApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FacebookApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Facebook API connection test successful");
                return "Facebook API connection successful";
            } else {
                log.warn("Facebook API connection test failed with status: {}", response.getStatusCode());
                return "Facebook API connection failed: " + response.getStatusCode();
            }

        } catch (Exception e) {
            log.error("Facebook API connection test failed: {}", e.getMessage());
            return "Facebook API connection failed: " + e.getMessage();
        }
    }

    /**
     * Check if Facebook API is healthy
     * @return true if API is accessible
     */
    public boolean isApiHealthy() {
        try {
            String result = testConnection();
            return result.contains("successful");
        } catch (Exception e) {
            log.error("Facebook API health check failed: {}", e.getMessage());
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
            FacebookApiResponse response = fetchOrders(dateString, 1, 1);

            if (response != null && response.getData() != null) {
                // Assuming API returns total count in response metadata
                // This might need adjustment based on actual Facebook API response structure
                return response.getData().getCount() != null ? response.getData().getCount() : 0;
            }

            return 0;
        } catch (Exception e) {
            log.error("Failed to get Facebook order count for date {}: {}", date, e.getMessage());
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

    private ResponseEntity<FacebookApiResponse> makeApiCallWithRetry(String url, HttpEntity<String> entity) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                attempts++;
                log.debug("Facebook API call attempt {} of {}", attempts, maxRetries);

                ResponseEntity<FacebookApiResponse> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, FacebookApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return response;
                }

                log.warn("Facebook API returned non-success status: {} on attempt {}",
                        response.getStatusCode(), attempts);

            } catch (HttpClientErrorException e) {
                lastException = e;
                if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500) {
                    // Client errors (4xx) - don't retry
                    log.error("Facebook API client error (4xx): {}", e.getMessage());
                    break;
                }
                log.warn("Facebook API server error on attempt {}: {}", attempts, e.getMessage());

            } catch (ResourceAccessException e) {
                lastException = e;
                log.warn("Facebook API timeout on attempt {}: {}", attempts, e.getMessage());

            } catch (Exception e) {
                lastException = e;
                log.warn("Facebook API unexpected error on attempt {}: {}", attempts, e.getMessage());
            }

            // Wait before retry (exponential backoff)
            if (attempts < maxRetries) {
                try {
                    int waitTime = 1000 * attempts; // 1s, 2s, 3s...
                    log.debug("Waiting {}ms before retry", waitTime);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry wait", ie);
                }
            }
        }

        log.error("Facebook API call failed after {} attempts", maxRetries);
        throw new RuntimeException("Facebook API call failed after " + maxRetries + " attempts", lastException);
    }
}