// OrderStatus.java - Order Status Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_order_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderStatusId.class)
public class OrderStatus {

    @Id
    @Column(name = "status_key")
    private Long statusKey;

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "transition_date_key")
    private Integer transitionDateKey;

    @Column(name = "transition_timestamp")
    private LocalDateTime transitionTimestamp;

    @Column(name = "duration_in_previous_status_hours")
    private Integer durationInPreviousStatusHours;

    @Column(name = "transition_reason")
    private String transitionReason;

    @Column(name = "transition_trigger")
    private String transitionTrigger;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "is_on_time_transition")
    private Boolean isOnTimeTransition;

    @Column(name = "is_expected_transition")
    private Boolean isExpectedTransition;

    @Column(name = "history_key")
    private Long historyKey;
}