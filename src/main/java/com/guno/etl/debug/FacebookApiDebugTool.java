// FacebookApiDebugTool.java - Debug tool for Facebook API issues
package com.guno.etl.debug;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Debug tool to analyze Facebook API response and fix DTO mapping issues
 * Use this to identify what's wrong with the JSON structure
 */
@Service
public class FacebookApiDebugTool {

    private static final Logger log = LoggerFactory.getLogger(FacebookApiDebugTool.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String authHeader;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public FacebookApiDebugTool(
            RestTemplate restTemplate,
            @Value("${etl.api.facebook.base-url}") String baseUrl,
            @Value("${etl.api.facebook.auth-header:}") String authHeader,
            @Value("${etl.api.facebook.api-key:}") String apiKey) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.authHeader = authHeader;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();

        log.info("FacebookApiDebugTool initialized");
        log.info("Base URL: {}", baseUrl);
        log.info("Auth Header: {}", authHeader);
        log.info("API Key: {}", apiKey != null ? "***configured***" : "NOT SET");
    }

    /**
     * Debug Facebook API by fetching raw response and analyzing structure
     */
    public void debugFacebookApi() {
        log.info("========================================");
        log.info("🔍 FACEBOOK API DEBUG ANALYSIS");
        log.info("========================================");

        try {
            // Test with yesterday's date
            String testDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            log.info("📅 Testing with date: {}", testDate);

            // Step 1: Test basic connectivity
            log.info("🔗 Step 1: Testing API connectivity...");
            testConnectivity();

            // Step 2: Fetch raw response
            log.info("📥 Step 2: Fetching raw API response...");
            String rawResponse = fetchRawResponse(testDate);

            if (rawResponse != null) {
                // Step 3: Analyze JSON structure
                log.info("🔍 Step 3: Analyzing JSON structure...");
                analyzeJsonStructure(rawResponse);

                // Step 4: Check specific fields
                log.info("🔍 Step 4: Checking DTO compatibility...");
                checkDtoCompatibility(rawResponse);

                // Step 5: Provide recommendations
                log.info("💡 Step 5: Recommendations...");
                provideRecommendations(rawResponse);
            }

        } catch (Exception e) {
            log.error("❌ Debug analysis failed: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("========================================");
        log.info("🏁 DEBUG ANALYSIS COMPLETED");
        log.info("========================================");
    }

    private void testConnectivity() {
        try {
            String testDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            String url = String.format("%s?date=%s&page=1&limit=1", baseUrl, testDate);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("🌐 Testing URL: {}", url);
            log.info("📋 Headers: {}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            log.info("✅ HTTP Status: {}", response.getStatusCode());
            log.info("📄 Content Type: {}", response.getHeaders().getContentType());
            log.info("📐 Content Length: {}", response.getHeaders().getContentLength());

        } catch (Exception e) {
            log.error("❌ Connectivity test failed: {}", e.getMessage());
        }
    }

    private String fetchRawResponse(String dateString) {
        try {
            String url = String.format("%s?date=%s&page=1&limit=5&filter-date=update",
                    baseUrl, dateString);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            String rawJson = response.getBody();

            log.info("📄 Raw JSON Response (first 500 chars):");
            log.info("{}", rawJson != null ? rawJson.substring(0, Math.min(500, rawJson.length())) + "..." : "NULL");

            return rawJson;

        } catch (Exception e) {
            log.error("❌ Failed to fetch raw response: {}", e.getMessage());
            return null;
        }
    }

    private void analyzeJsonStructure(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);

            log.info("🏗️ JSON Structure Analysis:");
            log.info("   Root Type: {}", rootNode.getNodeType());
            log.info("   Is Object: {}", rootNode.isObject());
            log.info("   Is Array: {}", rootNode.isArray());

            if (rootNode.isObject()) {
                log.info("📋 Root Level Fields:");
                rootNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = rootNode.get(fieldName);
                    log.info("   - {}: {} ({})", fieldName,
                            fieldValue.getNodeType(),
                            getFieldSample(fieldValue));
                });

                // Check specific expected fields
                checkExpectedField(rootNode, "status");
                checkExpectedField(rootNode, "message");
                checkExpectedField(rootNode, "code");
                checkExpectedField(rootNode, "data");

                // Analyze data field if exists
                if (rootNode.has("data")) {
                    analyzeDataField(rootNode.get("data"));
                }
            }

        } catch (Exception e) {
            log.error("❌ JSON structure analysis failed: {}", e.getMessage());
        }
    }

    private void analyzeDataField(JsonNode dataNode) {
        log.info("📊 Data Field Analysis:");
        log.info("   Data Type: {}", dataNode.getNodeType());

        if (dataNode.isObject()) {
            log.info("📋 Data Object Fields:");
            dataNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = dataNode.get(fieldName);
                log.info("   - {}: {} ({})", fieldName,
                        fieldValue.getNodeType(),
                        getFieldSample(fieldValue));
            });

            // Check for orders array
            if (dataNode.has("orders")) {
                analyzeOrdersArray(dataNode.get("orders"));
            }
        }
    }

    private void analyzeOrdersArray(JsonNode ordersNode) {
        log.info("📦 Orders Array Analysis:");
        log.info("   Is Array: {}", ordersNode.isArray());
        log.info("   Array Size: {}", ordersNode.size());

        if (ordersNode.isArray() && ordersNode.size() > 0) {
            JsonNode firstOrder = ordersNode.get(0);
            log.info("📋 First Order Fields:");
            firstOrder.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = firstOrder.get(fieldName);
                log.info("   - {}: {} ({})", fieldName,
                        fieldValue.getNodeType(),
                        getFieldSample(fieldValue));
            });

            // Check order data field
            if (firstOrder.has("data")) {
                analyzeOrderDataField(firstOrder.get("data"));
            }
        }
    }

    private void analyzeOrderDataField(JsonNode orderDataNode) {
        log.info("📊 Order Data Field Analysis:");
        log.info("   Data Type: {}", orderDataNode.getNodeType());

        if (orderDataNode.isObject()) {
            log.info("📋 Order Data Fields (first 20):");
            int count = 0;
            var fieldIterator = orderDataNode.fieldNames();
            while (fieldIterator.hasNext() && count < 20) {
                String fieldName = fieldIterator.next();
                JsonNode fieldValue = orderDataNode.get(fieldName);
                log.info("   - {}: {} ({})", fieldName,
                        fieldValue.getNodeType(),
                        getFieldSample(fieldValue));
                count++;
            }

            // Check critical fields
            checkExpectedField(orderDataNode, "id");
            checkExpectedField(orderDataNode, "total");
            checkExpectedField(orderDataNode, "cod");
            checkExpectedField(orderDataNode, "items");
            checkExpectedField(orderDataNode, "customer");
        }
    }

    private void checkDtoCompatibility(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);

            log.info("🔍 DTO Compatibility Check:");

            // Check FacebookApiResponse compatibility
            boolean hasStatus = rootNode.has("status");
            boolean hasMessage = rootNode.has("message");
            boolean hasCode = rootNode.has("code");
            boolean hasData = rootNode.has("data");

            log.info("   FacebookApiResponse fields:");
            log.info("     status: {} {}", hasStatus ? "✅" : "❌",
                    hasStatus ? "(" + rootNode.get("status").getNodeType() + ")" : "");
            log.info("     message: {} {}", hasMessage ? "✅" : "❌",
                    hasMessage ? "(" + rootNode.get("message").getNodeType() + ")" : "");
            log.info("     code: {} {}", hasCode ? "✅" : "❌",
                    hasCode ? "(" + rootNode.get("code").getNodeType() + ")" : "");
            log.info("     data: {} {}", hasData ? "✅" : "❌",
                    hasData ? "(" + rootNode.get("data").getNodeType() + ")" : "");

            if (hasData) {
                JsonNode dataNode = rootNode.get("data");
                boolean hasOrders = dataNode.has("orders");
                boolean hasCount = dataNode.has("count");
                boolean hasPage = dataNode.has("page");

                log.info("   FacebookDataWrapper fields:");
                log.info("     orders: {} {}", hasOrders ? "✅" : "❌",
                        hasOrders ? "(" + dataNode.get("orders").getNodeType() + ")" : "");
                log.info("     count: {} {}", hasCount ? "✅" : "❌",
                        hasCount ? "(" + dataNode.get("count").getNodeType() + ")" : "");
                log.info("     page: {} {}", hasPage ? "✅" : "❌",
                        hasPage ? "(" + dataNode.get("page").getNodeType() + ")" : "");
            }

        } catch (Exception e) {
            log.error("❌ DTO compatibility check failed: {}", e.getMessage());
        }
    }

    private void provideRecommendations(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);

            log.info("💡 Recommendations:");

            // Check if structure matches expected DTO
            if (!rootNode.has("status") || !rootNode.has("data")) {
                log.info("   🔧 ISSUE: Response structure doesn't match FacebookApiResponse");
                log.info("   📝 SOLUTION: Update DTO to match actual API response structure");

                // Suggest alternative structure
                log.info("   💡 Consider creating FacebookApiResponse with these fields:");
                rootNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = rootNode.get(fieldName);
                    String javaType = mapJsonTypeToJava(fieldValue);
                    log.info("      private {} {};", javaType, fieldName);
                });
            }

            // Check data wrapper structure
            if (rootNode.has("data")) {
                JsonNode dataNode = rootNode.get("data");
                if (!dataNode.has("orders")) {
                    log.info("   🔧 ISSUE: Data object doesn't contain 'orders' array");
                    log.info("   📝 SOLUTION: Check if orders are at a different path");

                    // Look for arrays that might contain orders
                    dataNode.fieldNames().forEachRemaining(fieldName -> {
                        JsonNode fieldValue = dataNode.get(fieldName);
                        if (fieldValue.isArray()) {
                            log.info("      🔍 Found array field: '{}' with {} items",
                                    fieldName, fieldValue.size());
                        }
                    });
                }
            }

            // Check for missing @JsonProperty annotations
            log.info("   🏷️ Ensure all DTO fields have @JsonProperty annotations");
            log.info("   🧪 Consider using @JsonIgnoreProperties(ignoreUnknown = true)");
            log.info("   🔄 Test with smaller response first (limit=1)");

        } catch (Exception e) {
            log.error("❌ Recommendations generation failed: {}", e.getMessage());
        }
    }

    // Helper methods
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authHeader != null && !authHeader.isEmpty() && apiKey != null && !apiKey.isEmpty()) {
            headers.set(authHeader, apiKey);
        }

        return headers;
    }

    private void checkExpectedField(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            JsonNode field = node.get(fieldName);
            log.info("   ✅ {}: {} ({})", fieldName, field.getNodeType(), getFieldSample(field));
        } else {
            log.info("   ❌ {}: MISSING", fieldName);
        }
    }

    private String getFieldSample(JsonNode node) {
        if (node.isNull()) {
            return "null";
        } else if (node.isArray()) {
            return "array[" + node.size() + "]";
        } else if (node.isObject()) {
            return "object{" + node.size() + " fields}";
        } else if (node.isTextual()) {
            String text = node.textValue();
            return "\"" + (text.length() > 20 ? text.substring(0, 20) + "..." : text) + "\"";
        } else {
            return node.toString();
        }
    }

    private String mapJsonTypeToJava(JsonNode node) {
        if (node.isInt()) return "Integer";
        if (node.isLong()) return "Long";
        if (node.isDouble()) return "Double";
        if (node.isBoolean()) return "Boolean";
        if (node.isTextual()) return "String";
        if (node.isArray()) return "List<Object>";
        if (node.isObject()) return "Object";
        return "Object";
    }
}