// Order.java
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "shop_id")
    private String shopId;

    @Column(name = "internal_uuid")
    private String internalUuid;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 1;

    @Column(name = "item_quantity", nullable = false)
    @Builder.Default
    private Integer itemQuantity = 0;

    @Column(name = "total_items_in_order", nullable = false)
    @Builder.Default
    private Integer totalItemsInOrder = 0;

    @Column(name = "gross_revenue", nullable = false)
    @Builder.Default
    private Double grossRevenue = 0.0;

    @Column(name = "net_revenue", nullable = false)
    @Builder.Default
    private Double netRevenue = 0.0;

    @Column(name = "shipping_fee", nullable = false)
    @Builder.Default
    private Double shippingFee = 0.0;

    @Column(name = "tax_amount", nullable = false)
    @Builder.Default
    private Double taxAmount = 0.0;

    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private Double discountAmount = 0.0;

    @Column(name = "cod_amount", nullable = false)
    @Builder.Default
    private Double codAmount = 0.0;

    @Column(name = "platform_fee", nullable = false)
    @Builder.Default
    private Double platformFee = 0.0;

    @Column(name = "seller_discount", nullable = false)
    @Builder.Default
    private Double sellerDiscount = 0.0;

    @Column(name = "platform_discount", nullable = false)
    @Builder.Default
    private Double platformDiscount = 0.0;

    @Column(name = "original_price", nullable = false)
    @Builder.Default
    private Double originalPrice = 0.0;

    @Column(name = "estimated_shipping_fee", nullable = false)
    @Builder.Default
    private Double estimatedShippingFee = 0.0;

    @Column(name = "actual_shipping_fee", nullable = false)
    @Builder.Default
    private Double actualShippingFee = 0.0;

    @Column(name = "shipping_weight_gram", nullable = false)
    @Builder.Default
    private Integer shippingWeightGram = 0;

    @Column(name = "days_to_ship", nullable = false)
    @Builder.Default
    private Integer daysToShip = 0;

    @Column(name = "is_delivered", nullable = false)
    @Builder.Default
    private Boolean isDelivered = false;

    @Column(name = "is_cancelled", nullable = false)
    @Builder.Default
    private Boolean isCancelled = false;

    @Column(name = "is_returned", nullable = false)
    @Builder.Default
    private Boolean isReturned = false;

    @Column(name = "is_cod", nullable = false)
    @Builder.Default
    private Boolean isCod = false;

    @Column(name = "is_new_customer", nullable = false)
    @Builder.Default
    private Boolean isNewCustomer = false;

    @Column(name = "is_repeat_customer", nullable = false)
    @Builder.Default
    private Boolean isRepeatCustomer = false;

    @Column(name = "is_bulk_order", nullable = false)
    @Builder.Default
    private Boolean isBulkOrder = false;

    @Column(name = "is_promotional_order", nullable = false)
    @Builder.Default
    private Boolean isPromotionalOrder = false;

    @Column(name = "is_same_day_delivery", nullable = false)
    @Builder.Default
    private Boolean isSameDayDelivery = false;

    @Column(name = "order_to_ship_hours", nullable = false)
    @Builder.Default
    private Integer orderToShipHours = 0;

    @Column(name = "ship_to_delivery_hours", nullable = false)
    @Builder.Default
    private Integer shipToDeliveryHours = 0;

    @Column(name = "total_fulfillment_hours", nullable = false)
    @Builder.Default
    private Integer totalFulfillmentHours = 0;

    @Column(name = "customer_order_sequence", nullable = false)
    @Builder.Default
    private Integer customerOrderSequence = 1;

    @Column(name = "customer_lifetime_orders", nullable = false)
    @Builder.Default
    private Integer customerLifetimeOrders = 1;

    @Column(name = "customer_lifetime_value", nullable = false)
    @Builder.Default
    private Double customerLifetimeValue = 0.0;

    @Column(name = "days_since_last_order", nullable = false)
    @Builder.Default
    private Integer daysSinceLastOrder = 0;

    @Column(name = "promotion_impact", nullable = false)
    @Builder.Default
    private Double promotionImpact = 0.0;

    @Column(name = "ad_revenue", nullable = false)
    @Builder.Default
    private Double adRevenue = 0.0;

    @Column(name = "organic_revenue", nullable = false)
    @Builder.Default
    private Double organicRevenue = 0.0;

    @Column(name = "aov", nullable = false)
    @Builder.Default
    private Double aov = 0.0;

    @Column(name = "shipping_cost_ratio", nullable = false)
    @Builder.Default
    private Double shippingCostRatio = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "raw_data")
    private Integer rawData;

    @Column(name = "platform_specific_data")
    private Integer platformSpecificData;

    // Foreign key relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
}