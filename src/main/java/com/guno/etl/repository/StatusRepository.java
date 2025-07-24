// StatusRepository.java - Status Repository
package com.guno.etl.repository;

import com.guno.etl.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    /**
     * Find status by platform and platform status code
     */
    Optional<Status> findByPlatformAndPlatformStatusCode(String platform, String platformStatusCode);

    /**
     * Find status by platform and platform status name
     */
    Optional<Status> findByPlatformAndPlatformStatusName(String platform, String platformStatusName);

    /**
     * Find status by standard status code
     */
    Optional<Status> findByStandardStatusCode(String standardStatusCode);

    /**
     * Find all statuses for a platform
     */
    List<Status> findByPlatformOrderByStatusKey(String platform);

    /**
     * Find statuses by category
     */
    List<Status> findByStatusCategory(String statusCategory);

    /**
     * Find or create status by platform and status code
     */
    @Query("SELECT s FROM Status s WHERE s.platform = :platform AND s.platformStatusCode = :statusCode")
    Optional<Status> findByPlatformAndStatusCode(@Param("platform") String platform,
                                                 @Param("statusCode") String statusCode);

    /**
     * Get next available status key for auto-generation
     */
    @Query("SELECT COALESCE(MAX(s.statusKey), 0) + 1 FROM Status s")
    Long findNextStatusKey();

    /**
     * Check if status exists for platform and status
     */
    boolean existsByPlatformAndPlatformStatusCode(String platform, String platformStatusCode);
}