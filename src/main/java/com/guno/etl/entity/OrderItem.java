package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_order_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderItemId.class)
public class OrderItem {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Id
    @Column(name = "sku")
    private String sku;

    @Id
    @Column(name = "platform_product_id")
    private String platformProductId;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "unit_price", nullable = false)
    @Builder.Default
    private Double unitPrice = 0.0;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Double totalPrice = 0.0;

    @Column(name = "item_discount", nullable = false)
    @Builder.Default
    private Double itemDiscount = 0.0;

    @Column(name = "promotion_type")
    private String promotionType;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "item_status")
    private String itemStatus;

    @Column(name = "item_sequence", nullable = false)
    @Builder.Default
    private Integer itemSequence = 1;

    @Column(name = "op_id", nullable = false)
    @Builder.Default
    private Long opId = 0L;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "sku", referencedColumnName = "sku", insertable = false, updatable = false),
            @JoinColumn(name = "platform_product_id", referencedColumnName = "platform_product_id", insertable = false, updatable = false)
    })
    private Product product;
}