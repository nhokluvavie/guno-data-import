package com.guno.etl.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParallelProcessingResult {

    // Overall processing info
    private boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private boolean parallelEnabled;

    // Individual platform results
    private EtlResult shopeeResult;
    private EtlResult tiktokResult;
    private EtlResult facebookResult;

    // Summary statistics
    private int totalOrders;
    private int totalProcessed;
    private int totalFailed;

    /**
     * Get processing duration in minutes
     */
    public long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Get processing duration in seconds
     */
    public long getDurationSeconds() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return 0;
    }

    /**
     * Calculate overall success rate
     */
    public double getSuccessRate() {
        if (totalOrders > 0) {
            return (double) totalProcessed / totalOrders * 100;
        }
        return 0.0;
    }

    /**
     * Check if any platform succeeded
     */
    public boolean hasAnySuccess() {
        return (shopeeResult != null && shopeeResult.isSuccess()) ||
                (tiktokResult != null && tiktokResult.isSuccess()) ||
                (facebookResult != null && facebookResult.isSuccess());
    }

    /**
     * Check if all enabled platforms succeeded
     */
    public boolean allPlatformsSucceeded() {
        boolean allSucceeded = true;

        if (shopeeResult != null) {
            allSucceeded &= shopeeResult.isSuccess();
        }

        if (tiktokResult != null) {
            allSucceeded &= tiktokResult.isSuccess();
        }

        if (facebookResult != null) {
            allSucceeded &= facebookResult.isSuccess();
        }

        return allSucceeded;
    }

    /**
     * Get formatted summary string
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("ETL %s: ", success ? "SUCCESS" : "FAILED"));
        summary.append(String.format("%d/%d orders processed ", totalProcessed, totalOrders));
        summary.append(String.format("(%.1f%%) ", getSuccessRate()));
        summary.append(String.format("in %d minutes", getDurationMinutes()));

        if (parallelEnabled) {
            summary.append(" [PARALLEL]");
        } else {
            summary.append(" [SEQUENTIAL]");
        }

        return summary.toString();
    }

    /**
     * Get detailed breakdown by platform
     */
    public String getDetailedSummary() {
        StringBuilder details = new StringBuilder();
        details.append(getSummary()).append("\n");

        if (shopeeResult != null) {
            details.append(String.format("  📱 Shopee: %s (%d/%d orders)\n",
                    shopeeResult.isSuccess() ? "SUCCESS" : "FAILED",
                    shopeeResult.getOrdersProcessed(),
                    shopeeResult.getTotalOrders()));
        }

        if (tiktokResult != null) {
            details.append(String.format("  🎵 TikTok: %s (%d/%d orders)\n",
                    tiktokResult.isSuccess() ? "SUCCESS" : "FAILED",
                    tiktokResult.getOrdersProcessed(),
                    tiktokResult.getTotalOrders()));
        }

        if (facebookResult != null) {
            details.append(String.format("  📘 Facebook: %s (%d/%d orders)\n",
                    facebookResult.isSuccess() ? "SUCCESS" : "FAILED",
                    facebookResult.getOrdersProcessed(),
                    facebookResult.getTotalOrders()));
        }

        return details.toString();
    }
}