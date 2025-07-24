// PaymentInfo.java - Payment Info Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_payment_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "payment_key")
    private Long paymentKey;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_category")
    private String paymentCategory;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "is_cod")
    private Boolean isCod;

    @Column(name = "is_prepaid")
    private Boolean isPrepaid;

    @Column(name = "is_installment")
    private Boolean isInstallment;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "supports_refund")
    private Boolean supportsRefund;

    @Column(name = "supports_partial_refund")
    private Boolean supportsPartialRefund;

    @Column(name = "refund_processing_days")
    private Integer refundProcessingDays;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "requires_verification")
    private Boolean requiresVerification;

    @Column(name = "fraud_score")
    private Double fraudScore;

    @Column(name = "transaction_fee_rate")
    private Double transactionFeeRate;

    @Column(name = "processing_fee")
    private Double processingFee;

    @Column(name = "payment_processing_time_minutes")
    private Integer paymentProcessingTimeMinutes;

    @Column(name = "settlement_days")
    private Integer settlementDays;
}