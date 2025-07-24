package com.guno.etl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name = "customer_id")
    @NotBlank(message = "Customer ID cannot be blank")
    @Size(max = 255, message = "Customer ID must not exceed 255 characters")
    private String customerId;

    @Column(name = "customer_key", nullable = false)
    @NotNull(message = "Customer key cannot be null")
    @Positive(message = "Customer key must be positive")
    private Long customerKey;

    @Column(name = "platform_customer_id")
    @Size(max = 255, message = "Platform customer ID must not exceed 255 characters")
    private String platformCustomerId;

    @Column(name = "phone_hash")
    @Size(max = 255, message = "Phone hash must not exceed 255 characters")
    private String phoneHash;

    @Column(name = "email_hash")
    @Size(max = 255, message = "Email hash must not exceed 255 characters")
    private String emailHash;

    @Column(name = "gender")
    @Pattern(regexp = "male|female|other|unknown", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Gender must be one of: male, female, other, unknown")
    private String gender;

    @Column(name = "age_group")
    @Pattern(regexp = "18-25|26-35|36-45|46-55|55+|unknown",
            message = "Age group must be valid range")
    private String ageGroup;

    @Column(name = "customer_segment")
    @Size(max = 255, message = "Customer segment must not exceed 255 characters")
    private String customerSegment;

    @Column(name = "customer_tier")
    @Pattern(regexp = "BRONZE|SILVER|GOLD|PLATINUM|VIP|STANDARD", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Customer tier must be valid tier")
    private String customerTier;

    @Column(name = "acquisition_channel")
    @Size(max = 255, message = "Acquisition channel must not exceed 255 characters")
    private String acquisitionChannel;

    @Column(name = "first_order_date")
    @PastOrPresent(message = "First order date cannot be in the future")
    private LocalDateTime firstOrderDate;

    @Column(name = "last_order_date")
    @PastOrPresent(message = "Last order date cannot be in the future")
    private LocalDateTime lastOrderDate;

    @Column(name = "total_orders", nullable = false)
    @NotNull(message = "Total orders cannot be null")
    @Min(value = 0, message = "Total orders must be non-negative")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_spent", nullable = false)
    @NotNull(message = "Total spent cannot be null")
    @DecimalMin(value = "0.0", message = "Total spent must be non-negative")
    @Builder.Default
    private Double totalSpent = 0.0;

    @Column(name = "average_order_value", nullable = false)
    @NotNull(message = "Average order value cannot be null")
    @DecimalMin(value = "0.0", message = "Average order value must be non-negative")
    @Builder.Default
    private Double averageOrderValue = 0.0;

    @Column(name = "total_items_purchased", nullable = false)
    @NotNull(message = "Total items purchased cannot be null")
    @Min(value = 0, message = "Total items purchased must be non-negative")
    @Builder.Default
    private Integer totalItemsPurchased = 0;

    @Column(name = "days_since_first_order", nullable = false)
    @NotNull(message = "Days since first order cannot be null")
    @Min(value = 0, message = "Days since first order must be non-negative")
    @Builder.Default
    private Integer daysSinceFirstOrder = 0;

    @Column(name = "days_since_last_order", nullable = false)
    @NotNull(message = "Days since last order cannot be null")
    @Min(value = 0, message = "Days since last order must be non-negative")
    @Builder.Default
    private Integer daysSinceLastOrder = 0;

    @Column(name = "purchase_frequency_days", nullable = false)
    @NotNull(message = "Purchase frequency days cannot be null")
    @DecimalMin(value = "0.0", message = "Purchase frequency days must be non-negative")
    @Builder.Default
    private Double purchaseFrequencyDays = 0.0;

    @Column(name = "return_rate", nullable = false)
    @NotNull(message = "Return rate cannot be null")
    @DecimalMin(value = "0.0", message = "Return rate must be non-negative")
    @DecimalMax(value = "1.0", message = "Return rate must not exceed 1.0")
    @Builder.Default
    private Double returnRate = 0.0;

    @Column(name = "cancellation_rate", nullable = false)
    @NotNull(message = "Cancellation rate cannot be null")
    @DecimalMin(value = "0.0", message = "Cancellation rate must be non-negative")
    @DecimalMax(value = "1.0", message = "Cancellation rate must not exceed 1.0")
    @Builder.Default
    private Double cancellationRate = 0.0;

    @Column(name = "cod_preference_rate", nullable = false)
    @NotNull(message = "COD preference rate cannot be null")
    @DecimalMin(value = "0.0", message = "COD preference rate must be non-negative")
    @DecimalMax(value = "1.0", message = "COD preference rate must not exceed 1.0")
    @Builder.Default
    private Double codPreferenceRate = 0.0;

    @Column(name = "favorite_category")
    @Size(max = 255, message = "Favorite category must not exceed 255 characters")
    private String favoriteCategory;

    @Column(name = "favorite_brand")
    @Size(max = 255, message = "Favorite brand must not exceed 255 characters")
    private String favoriteBrand;

    @Column(name = "preferred_payment_method")
    @Size(max = 255, message = "Preferred payment method must not exceed 255 characters")
    private String preferredPaymentMethod;

    @Column(name = "preferred_platform")
    @Pattern(regexp = "SHOPEE|TIKTOK|FACEBOOK|LAZADA|OTHERS", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Preferred platform must be valid platform")
    private String preferredPlatform;

    @Column(name = "primary_shipping_province")
    @Size(max = 255, message = "Primary shipping province must not exceed 255 characters")
    private String primaryShippingProvince;

    @Column(name = "ships_to_multiple_provinces", nullable = false)
    @NotNull(message = "Ships to multiple provinces flag cannot be null")
    @Builder.Default
    private Boolean shipsToMultipleProvinces = false;

    @Column(name = "loyalty_points", nullable = false)
    @NotNull(message = "Loyalty points cannot be null")
    @Min(value = 0, message = "Loyalty points must be non-negative")
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(name = "referral_count", nullable = false)
    @NotNull(message = "Referral count cannot be null")
    @Min(value = 0, message = "Referral count must be non-negative")
    @Builder.Default
    private Integer referralCount = 0;

    @Column(name = "is_referrer", nullable = false)
    @NotNull(message = "Is referrer flag cannot be null")
    @Builder.Default
    private Boolean isReferrer = false;

    /**
     * Business logic validation
     */
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Validate total orders vs average order value
        if (totalOrders > 0 && totalSpent > 0) {
            double calculatedAov = totalSpent / totalOrders;
            if (Math.abs(averageOrderValue - calculatedAov) > 0.01) {
                averageOrderValue = calculatedAov;
            }
        }

        // Validate date consistency
        if (firstOrderDate != null && lastOrderDate != null &&
                firstOrderDate.isAfter(lastOrderDate)) {
            throw new IllegalStateException("First order date cannot be after last order date");
        }

        // Validate rates are between 0 and 1
        validateRate(returnRate, "Return rate");
        validateRate(cancellationRate, "Cancellation rate");
        validateRate(codPreferenceRate, "COD preference rate");
    }

    private void validateRate(Double rate, String rateName) {
        if (rate != null && (rate < 0.0 || rate > 1.0)) {
            throw new IllegalArgumentException(rateName + " must be between 0.0 and 1.0");
        }
    }

    /**
     * Calculate days since first order
     */
    public void updateDaysSinceFirstOrder() {
        if (firstOrderDate != null) {
            daysSinceFirstOrder = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    firstOrderDate.toLocalDate(), LocalDateTime.now().toLocalDate());
        }
    }

    /**
     * Calculate days since last order
     */
    public void updateDaysSinceLastOrder() {
        if (lastOrderDate != null) {
            daysSinceLastOrder = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    lastOrderDate.toLocalDate(), LocalDateTime.now().toLocalDate());
        }
    }

    /**
     * Calculate purchase frequency
     */
    public void updatePurchaseFrequency() {
        if (totalOrders > 1 && firstOrderDate != null && lastOrderDate != null) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                    firstOrderDate.toLocalDate(), lastOrderDate.toLocalDate());
            purchaseFrequencyDays = (double) totalDays / (totalOrders - 1);
        }
    }

    /**
     * Update all calculated fields
     */
    public void recalculateMetrics() {
        updateDaysSinceFirstOrder();
        updateDaysSinceLastOrder();
        updatePurchaseFrequency();

        if (totalOrders > 0 && totalSpent > 0) {
            averageOrderValue = totalSpent / totalOrders;
        }
    }
}