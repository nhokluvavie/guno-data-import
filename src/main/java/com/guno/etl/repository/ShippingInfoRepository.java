// ShippingInfoRepository.java - Shipping Info Repository
package com.guno.etl.repository;

import com.guno.etl.entity.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, String> {

    /**
     * Find shipping info by provider name
     */
    List<ShippingInfo> findByProviderName(String providerName);

    /**
     * Find shipping info by provider ID
     */
    List<ShippingInfo> findByProviderId(String providerId);

    /**
     * Find shipping info by provider type
     */
    List<ShippingInfo> findByProviderType(String providerType);

    /**
     * Find shipping info by service type
     */
    List<ShippingInfo> findByServiceType(String serviceType);

    /**
     * Find shipping info by delivery type
     */
    List<ShippingInfo> findByDeliveryType(String deliveryType);

    /**
     * Find COD supporting providers
     */
    List<ShippingInfo> findBySupportsCodTrue();

    /**
     * Find providers with tracking
     */
    List<ShippingInfo> findByProvidesTrackingTrue();

    /**
     * Find providers with SMS updates
     */
    List<ShippingInfo> findByProvidesSmsUpdatesTrue();

    /**
     * Find nationwide coverage providers
     */
    List<ShippingInfo> findByCoverageNationwideTrue();

    /**
     * Find international coverage providers
     */
    List<ShippingInfo> findByCoverageInternationalTrue();

    /**
     * Find providers by average delivery days range
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.averageDeliveryDays BETWEEN :minDays AND :maxDays")
    List<ShippingInfo> findByAverageDeliveryDaysRange(@Param("minDays") Double minDays,
                                                      @Param("maxDays") Double maxDays);

    /**
     * Find providers by on-time delivery rate
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.onTimeDeliveryRate >= :minRate ORDER BY si.onTimeDeliveryRate DESC")
    List<ShippingInfo> findByOnTimeDeliveryRateAbove(@Param("minRate") Double minRate);

    /**
     * Find providers by success delivery rate
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.successDeliveryRate >= :minRate ORDER BY si.successDeliveryRate DESC")
    List<ShippingInfo> findBySuccessDeliveryRateAbove(@Param("minRate") Double minRate);

    /**
     * Find providers by damage rate below threshold
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.damageRate <= :maxRate ORDER BY si.damageRate ASC")
    List<ShippingInfo> findByDamageRateBelow(@Param("maxRate") Double maxRate);

    /**
     * Find providers by base fee range
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.baseFee BETWEEN :minFee AND :maxFee")
    List<ShippingInfo> findByBaseFeeRange(@Param("minFee") Double minFee,
                                          @Param("maxFee") Double maxFee);

    /**
     * Get next shipping key for auto-generation
     */
    @Query("SELECT COALESCE(MAX(si.shippingKey), 0) + 1 FROM ShippingInfo si")
    Long findNextShippingKey();

    /**
     * Count orders by provider
     */
    @Query("SELECT si.providerName, COUNT(si) FROM ShippingInfo si GROUP BY si.providerName")
    List<Object[]> countOrdersByProvider();

    /**
     * Find best performing providers
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.onTimeDeliveryRate >= 0.9 " +
            "AND si.successDeliveryRate >= 0.95 AND si.damageRate <= 0.05 " +
            "ORDER BY si.onTimeDeliveryRate DESC, si.successDeliveryRate DESC")
    List<ShippingInfo> findBestPerformingProviders();

    /**
     * Find providers covering specific province
     */
    @Query("SELECT si FROM ShippingInfo si WHERE si.coverageProvinces LIKE %:province% OR si.coverageNationwide = true")
    List<ShippingInfo> findProvidersCoveringProvince(@Param("province") String province);
}