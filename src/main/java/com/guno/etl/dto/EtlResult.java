package com.guno.etl.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EtlResult {

    // Core processing info
    private boolean success;
    private int totalOrders;
    private int ordersProcessed;
    private String errorMessage;

    // Timing info
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;

    // Error tracking
    private List<FailedOrder> failedOrders = new ArrayList<>();

    /**
     * Calculate duration between start and end time
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (totalOrders > 0) {
            return (double) ordersProcessed / totalOrders * 100;
        }
        return 0.0;
    }

    /**
     * Increment processed orders count
     */
    public void incrementProcessed() {
        this.ordersProcessed++;
    }

    /**
     * Add failed order to the list
     */
    public void addFailedOrder(String orderId, String errorMessage) {
        this.failedOrders.add(new FailedOrder(orderId, errorMessage));
    }

    /**
     * Get formatted duration string
     */
    public String getFormattedDuration() {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.1fs", durationMs / 1000.0);
        } else {
            return String.format("%.1fm", durationMs / 60000.0);
        }
    }

    /**
     * Get summary string
     */
    public String getSummary() {
        return String.format("%s: %d/%d orders (%.1f%%) in %s",
                success ? "SUCCESS" : "FAILED",
                ordersProcessed,
                totalOrders,
                getSuccessRate(),
                getFormattedDuration());
    }

    /**
     * Check if there are any failed orders
     */
    public boolean hasFailures() {
        return !failedOrders.isEmpty();
    }

    /**
     * Get count of failed orders
     */
    public int getFailedCount() {
        return failedOrders.size();
    }
}