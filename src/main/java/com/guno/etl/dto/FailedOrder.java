package com.guno.etl.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FailedOrder {

    private String orderId;
    private String errorMessage;
    private LocalDateTime failureTime;
    private String platform;

    // Constructor for backward compatibility
    public FailedOrder(String orderId, String errorMessage) {
        this.orderId = orderId;
        this.errorMessage = errorMessage;
        this.failureTime = LocalDateTime.now();
    }

    // Constructor with platform
    public FailedOrder(String orderId, String errorMessage, String platform) {
        this.orderId = orderId;
        this.errorMessage = errorMessage;
        this.platform = platform;
        this.failureTime = LocalDateTime.now();
    }

    /**
     * Get formatted error description
     */
    public String getFormattedError() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ").append(orderId);

        if (platform != null) {
            sb.append(" (").append(platform).append(")");
        }

        sb.append(": ").append(errorMessage);

        if (failureTime != null) {
            sb.append(" at ").append(failureTime.toString());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedError();
    }
}