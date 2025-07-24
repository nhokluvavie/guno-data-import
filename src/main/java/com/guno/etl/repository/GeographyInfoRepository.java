package com.guno.etl.repository;

import com.guno.etl.entity.GeographyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeographyInfoRepository extends JpaRepository<GeographyInfo, String> {

    /**
     * Find by province name
     */
    List<GeographyInfo> findByProvinceName(String provinceName);

    /**
     * Find by district name
     */
    List<GeographyInfo> findByDistrictName(String districtName);

    /**
     * Find urban areas
     */
    List<GeographyInfo> findByIsUrbanTrue();

    /**
     * Find metropolitan areas
     */
    List<GeographyInfo> findByIsMetropolitanTrue();

    /**
     * Find by shipping zone
     */
    List<GeographyInfo> findByShippingZone(String shippingZone);

    /**
     * Find areas with express delivery
     */
    List<GeographyInfo> findByExpressDeliveryAvailableTrue();

    /**
     * Find next available geography key
     */
    @Query("SELECT COALESCE(MAX(g.geographyKey), 0) + 1 FROM GeographyInfo g")
    Long findNextGeographyKey();

    /**
     * Find by delivery complexity
     */
    List<GeographyInfo> findByDeliveryComplexity(String deliveryComplexity);
}