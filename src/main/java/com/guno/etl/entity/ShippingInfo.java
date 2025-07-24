// ShippingInfo.java - Shipping Info Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_shipping_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "shipping_key")
    private Long shippingKey;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_type")
    private String providerType;

    @Column(name = "provider_tier")
    private String providerTier;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "service_tier")
    private String serviceTier;

    @Column(name = "delivery_commitment")
    private String deliveryCommitment;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "pickup_type")
    private String pickupType;

    @Column(name = "delivery_type")
    private String deliveryType;

    @Column(name = "base_fee")
    private Double baseFee;

    @Column(name = "weight_based_fee")
    private Double weightBasedFee;

    @Column(name = "distance_based_fee")
    private Double distanceBasedFee;

    @Column(name = "cod_fee")
    private Double codFee;

    @Column(name = "insurance_fee")
    private Double insuranceFee;

    @Column(name = "supports_cod")
    private Boolean supportsCod;

    @Column(name = "supports_insurance")
    private Boolean supportsInsurance;

    @Column(name = "supports_fragile")
    private Boolean supportsFragile;

    @Column(name = "supports_refrigerated")
    private Boolean supportsRefrigerated;

    @Column(name = "provides_tracking")
    private Boolean providesTracking;

    @Column(name = "provides_sms_updates")
    private Boolean providesSmsUpdates;

    @Column(name = "average_delivery_days")
    private Double averageDeliveryDays;

    @Column(name = "on_time_delivery_rate")
    private Double onTimeDeliveryRate;

    @Column(name = "success_delivery_rate")
    private Double successDeliveryRate;

    @Column(name = "damage_rate")
    private Double damageRate;

    @Column(name = "coverage_provinces")
    private String coverageProvinces;

    @Column(name = "coverage_nationwide")
    private Boolean coverageNationwide;

    @Column(name = "coverage_international")
    private Boolean coverageInternational;
}