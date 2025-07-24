// StatusMappingInitializer.java - Auto Status Mapping Setup
package com.guno.etl.config;

import com.guno.etl.entity.Status;
import com.guno.etl.repository.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Auto-initialize status mappings for all platforms on application startup
 * This prevents Hibernate "null id" errors when creating status entities during ETL
 */
@Component
public class StatusMappingInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StatusMappingInitializer.class);

    @Autowired
    private StatusRepository statusRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Initializing Status Mappings ===");

        try {
            initializeShopeeStatusMappings();
            initializeTikTokStatusMappings();
            initializeFacebookStatusMappings();

            log.info("✅ Status mappings initialization completed successfully");

        } catch (Exception e) {
            log.error("❌ Failed to initialize status mappings", e);
            // Don't throw exception - let application start anyway
        }
    }

    private void initializeShopeeStatusMappings() {
        String platform = "SHOPEE";
        log.info("Initializing {} status mappings...", platform);

        List<StatusMapping> shopeeMappings = Arrays.asList(
                new StatusMapping("UNPAID", "UNPAID", "PENDING"),
                new StatusMapping("TO_PROCESS", "PROCESSING", "PROCESSING"),
                new StatusMapping("PROCESSED", "PROCESSED", "PROCESSING"),
                new StatusMapping("TO_SHIP", "READY_TO_SHIP", "PROCESSING"),
                new StatusMapping("SHIPPED", "SHIPPING", "PROCESSING"),
                new StatusMapping("TO_RECEIVE", "SHIPPED", "PROCESSING"),
                new StatusMapping("COMPLETED", "COMPLETED", "FINAL"),
                new StatusMapping("CANCELLED", "CANCELLED", "FINAL"),
                new StatusMapping("IN_CANCEL", "CANCELLING", "PROCESSING"),
                new StatusMapping("RETURNED", "RETURNED", "FINAL")
        );

        createStatusMappingsIfNotExist(platform, shopeeMappings);
    }

    private void initializeTikTokStatusMappings() {
        String platform = "TIKTOK";
        log.info("Initializing {} status mappings...", platform);

        List<StatusMapping> tiktokMappings = Arrays.asList(
                new StatusMapping("DELIVERED", "COMPLETED", "FINAL"),
                new StatusMapping("CANCELLED", "CANCELLED", "FINAL"),
                new StatusMapping("AWAITING_SHIPMENT", "READY_TO_SHIP", "PROCESSING"),
                new StatusMapping("IN_TRANSIT", "SHIPPING", "PROCESSING"),
                new StatusMapping("AWAITING_PAYMENT", "UNPAID", "PENDING"),
                new StatusMapping("PROCESSING", "PROCESSING", "PROCESSING"),
                new StatusMapping("CONFIRMED", "CONFIRMED", "PROCESSING"),
                new StatusMapping("READY_TO_SHIP", "READY_TO_SHIP", "PROCESSING"),
                new StatusMapping("SHIPPED", "SHIPPING", "PROCESSING"),
                new StatusMapping("RETURNED", "RETURNED", "FINAL")
        );

        createStatusMappingsIfNotExist(platform, tiktokMappings);
    }

    private void initializeFacebookStatusMappings() {
        String platform = "FACEBOOK";
        log.info("Initializing {} status mappings...", platform);

        List<StatusMapping> facebookMappings = Arrays.asList(
                new StatusMapping("PENDING", "PENDING", "PENDING"),
                new StatusMapping("CONFIRMED", "CONFIRMED", "PROCESSING"),
                new StatusMapping("SHIPPED", "SHIPPING", "PROCESSING"),
                new StatusMapping("DELIVERED", "COMPLETED", "FINAL"),
                new StatusMapping("CANCELLED", "CANCELLED", "FINAL"),
                new StatusMapping("RETURNED", "RETURNED", "FINAL")
        );

        createStatusMappingsIfNotExist(platform, facebookMappings);
    }

    private void createStatusMappingsIfNotExist(String platform, List<StatusMapping> mappings) {
        int createdCount = 0;
        int existingCount = 0;

        for (StatusMapping mapping : mappings) {
            try {
                boolean exists = statusRepository.existsByPlatformAndPlatformStatusCode(
                        platform, mapping.platformStatusCode);

                if (!exists) {
                    Status status = Status.builder()
                            .platform(platform)
                            .platformStatusCode(mapping.platformStatusCode)
                            .platformStatusName(mapping.platformStatusCode)
                            .standardStatusCode(mapping.standardStatusCode)
                            .standardStatusName(mapping.standardStatusCode)
                            .statusCategory(mapping.statusCategory)
                            .build();

                    statusRepository.save(status);
                    createdCount++;

                    log.debug("Created status mapping: {} {} -> {}",
                            platform, mapping.platformStatusCode, mapping.standardStatusCode);
                } else {
                    existingCount++;
                }

            } catch (Exception e) {
                log.warn("Failed to create status mapping: {} {} -> {}",
                        platform, mapping.platformStatusCode, mapping.standardStatusCode, e);
            }
        }

        log.info("✅ {} status mappings: {} created, {} already existed",
                platform, createdCount, existingCount);
    }

    /**
     * Helper class for status mapping data
     */
    private static class StatusMapping {
        final String platformStatusCode;
        final String standardStatusCode;
        final String statusCategory;

        StatusMapping(String platformStatusCode, String standardStatusCode, String statusCategory) {
            this.platformStatusCode = platformStatusCode;
            this.standardStatusCode = standardStatusCode;
            this.statusCategory = statusCategory;
        }
    }
}