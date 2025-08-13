package com.guno.etl.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BatchProcessingResult {

    // Overall processing info
    private boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;

    // Entity-specific processing counts
    private int customersProcessed;
    private int ordersProcessed;
    private int itemsProcessed;
    private int productsProcessed;
    private int geographyProcessed;
    private int paymentsProcessed;
    private int shippingProcessed;
    private int dateInfoProcessed;
    private int statusProcessed;

    /**
     * Get processing duration in milliseconds
     */
    public long getDurationMs() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }

    /**
     * Get processing duration in seconds
     */
    public long getDurationSeconds() {
        return getDurationMs() / 1000;
    }

    /**
     * Get processing duration in minutes
     */
    public long getDurationMinutes() {
        return getDurationSeconds() / 60;
    }

    /**
     * Get total records processed across all entities
     */
    public int getTotalRecordsProcessed() {
        return customersProcessed + ordersProcessed + itemsProcessed +
                productsProcessed + geographyProcessed + paymentsProcessed +
                shippingProcessed + dateInfoProcessed + statusProcessed;
    }

    /**
     * Get processing rate (records per second)
     */
    public double getProcessingRate() {
        long durationSec = getDurationSeconds();
        if (durationSec > 0) {
            return (double) getTotalRecordsProcessed() / durationSec;
        }
        return 0.0;
    }

    /**
     * Get formatted summary string
     */
    public String getSummary() {
        return String.format("Batch %s: %d total records in %d seconds (%.1f records/sec)",
                success ? "SUCCESS" : "FAILED",
                getTotalRecordsProcessed(),
                getDurationSeconds(),
                getProcessingRate());
    }

    /**
     * Get detailed breakdown by entity type
     */
    public String getDetailedSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getSummary()).append("\n");
        summary.append("Entity Breakdown:\n");
        summary.append(String.format("  👥 Customers: %d\n", customersProcessed));
        summary.append(String.format("  📦 Orders: %d\n", ordersProcessed));
        summary.append(String.format("  📋 Items: %d\n", itemsProcessed));
        summary.append(String.format("  🏷️ Products: %d\n", productsProcessed));
        summary.append(String.format("  🌍 Geography: %d\n", geographyProcessed));
        summary.append(String.format("  💳 Payments: %d\n", paymentsProcessed));
        summary.append(String.format("  🚚 Shipping: %d\n", shippingProcessed));
        summary.append(String.format("  📅 Date Info: %d\n", dateInfoProcessed));
        summary.append(String.format("  📊 Status: %d\n", statusProcessed));
        return summary.toString();
    }

    /**
     * Check if processing was successful
     */
    public boolean isSuccessful() {
        return success && getTotalRecordsProcessed() > 0;
    }

    /**
     * Get performance level based on processing rate
     */
    public String getPerformanceLevel() {
        double rate = getProcessingRate();
        if (rate >= 1000) {
            return "EXCELLENT";
        } else if (rate >= 500) {
            return "GOOD";
        } else if (rate >= 100) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
}