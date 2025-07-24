// GeographyInfo.java
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_geography_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographyInfo {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "geography_key", nullable = false)
    @Builder.Default
    private Long geographyKey = 0L;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "province_name")
    private String provinceName;

    @Column(name = "province_type")
    private String provinceType;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "district_type")
    private String districtType;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "ward_name")
    private String wardName;

    @Column(name = "ward_type")
    private String wardType;

    @Column(name = "is_urban", nullable = false)
    @Builder.Default
    private Boolean isUrban = false;

    @Column(name = "is_metropolitan", nullable = false)
    @Builder.Default
    private Boolean isMetropolitan = false;

    @Column(name = "is_coastal", nullable = false)
    @Builder.Default
    private Boolean isCoastal = false;

    @Column(name = "is_border", nullable = false)
    @Builder.Default
    private Boolean isBorder = false;

    @Column(name = "economic_tier")
    private String economicTier;

    @Column(name = "population_density")
    private String populationDensity;

    @Column(name = "income_level")
    private String incomeLevel;

    @Column(name = "shipping_zone")
    private String shippingZone;

    @Column(name = "delivery_complexity")
    private String deliveryComplexity;

    @Column(name = "standard_delivery_days", nullable = false)
    @Builder.Default
    private Integer standardDeliveryDays = 0;

    @Column(name = "express_delivery_available", nullable = false)
    @Builder.Default
    private Boolean expressDeliveryAvailable = false;

    @Column(name = "latitude", nullable = false)
    @Builder.Default
    private Double latitude = 0.0;

    @Column(name = "longitude", nullable = false)
    @Builder.Default
    private Double longitude = 0.0;

    // Foreign key relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
}