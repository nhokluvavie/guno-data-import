// OrderStatusDetail.java - Order Status Detail Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_order_status_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderStatusId.class)
public class OrderStatusDetail {

    @Id
    @Column(name = "status_key")
    private Long statusKey;

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "is_active_order")
    private Boolean isActiveOrder;

    @Column(name = "is_completed_order")
    private Boolean isCompletedOrder;

    @Column(name = "is_revenue_recognized")
    private Boolean isRevenueRecognized;

    @Column(name = "is_refundable")
    private Boolean isRefundable;

    @Column(name = "is_cancellable")
    private Boolean isCancellable;

    @Column(name = "is_trackable")
    private Boolean isTrackable;

    @Column(name = "next_possible_statuses")
    private String nextPossibleStatuses;

    @Column(name = "auto_transition_hours")
    private Integer autoTransitionHours;

    @Column(name = "requires_manual_action")
    private Boolean requiresManualAction;

    @Column(name = "status_color")
    private String statusColor;

    @Column(name = "status_icon")
    private String statusIcon;

    @Column(name = "customer_visible")
    private Boolean customerVisible;

    @Column(name = "customer_description")
    private String customerDescription;

    @Column(name = "average_duration_hours")
    private Double averageDurationHours;

    @Column(name = "success_rate")
    private Double successRate;
}