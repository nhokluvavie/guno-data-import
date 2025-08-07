// MultiPlatformIntegrationTest.java - OPTIMIZED for new scheduler
package com.guno.etl.integration;

import com.guno.etl.service.*;
import com.guno.etl.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MultiPlatformIntegrationTest {

    @Autowired
    private MultiPlatformScheduler scheduler;

    @Autowired(required = false)
    private ShopeeEtlService shopeeEtlService;

    @Autowired(required = false)
    private TikTokEtlService tiktokEtlService;

    @Autowired(required = false)
    private FacebookEtlService facebookEtlService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Test
    @DisplayName("1. Test Scheduler Configuration")
    public void testSchedulerConfiguration() {
        System.out.println("\n=== TEST 1: Scheduler Configuration ===");

        assertNotNull(scheduler, "Scheduler should be available");

        Map<String, Object> stats = scheduler.getSchedulerStatistics();
        assertNotNull(stats, "Scheduler statistics should be available");

        System.out.println("✅ Scheduler enabled: " + stats.get("schedulerEnabled"));
        System.out.println("✅ Parallel execution: " + stats.get("parallelExecution"));
        System.out.println("✅ Total executions: " + stats.get("totalExecutions"));
    }

    @Test
    @DisplayName("2. Test Individual Platform Triggers")
    public void testIndividualPlatformTriggers() {
        System.out.println("\n=== TEST 2: Individual Platform Triggers ===");

        long ordersBefore = orderRepository.count();

        // Test Shopee trigger
        if (shopeeEtlService != null) {
            boolean shopeeResult = scheduler.triggerPlatform("SHOPEE");
            System.out.println("✅ Shopee trigger result: " + shopeeResult);
        }

        // Test TikTok trigger
        if (tiktokEtlService != null) {
            boolean tiktokResult = scheduler.triggerPlatform("TIKTOK");
            System.out.println("✅ TikTok trigger result: " + tiktokResult);
        }

        // Test Facebook trigger
        if (facebookEtlService != null) {
            boolean facebookResult = scheduler.triggerPlatform("FACEBOOK");
            System.out.println("✅ Facebook trigger result: " + facebookResult);
        }

        long ordersAfter = orderRepository.count();
        System.out.println("✅ Orders processed: " + (ordersAfter - ordersBefore));
    }

    @Test
    @DisplayName("3. Test All Platforms Trigger")
    public void testAllPlatformsTrigger() {
        System.out.println("\n=== TEST 3: All Platforms Trigger ===");

        long customersBefore = customerRepository.count();
        long ordersBefore = orderRepository.count();

        // Trigger all platforms
        scheduler.triggerAllPlatforms();

        // Wait for completion
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long customersAfter = customerRepository.count();
        long ordersAfter = orderRepository.count();

        System.out.println("✅ Customers: " + customersBefore + " → " + customersAfter);
        System.out.println("✅ Orders: " + ordersBefore + " → " + ordersAfter);

        assertTrue(customersAfter >= customersBefore, "Customer count should not decrease");
        assertTrue(ordersAfter >= ordersBefore, "Order count should not decrease");
    }

    @Test
    @DisplayName("4. Test Platform Statistics")
    public void testPlatformStatistics() {
        System.out.println("\n=== TEST 4: Platform Statistics ===");

        // Trigger some executions first
        scheduler.triggerAllPlatforms();

        Map<String, Object> stats = scheduler.getSchedulerStatistics();

        @SuppressWarnings("unchecked")
        Map<String, Object> platformStats = (Map<String, Object>) stats.get("platforms");

        System.out.println("📊 Platform Statistics:");

        for (String platform : new String[]{"SHOPEE", "TIKTOK", "FACEBOOK"}) {
            @SuppressWarnings("unchecked")
            Map<String, Object> platformData = (Map<String, Object>) platformStats.get(platform);

            if (platformData != null) {
                System.out.println("   " + platform + ":");
                System.out.println("     Enabled: " + platformData.get("enabled"));
                System.out.println("     Success Count: " + platformData.get("successCount"));
                System.out.println("     Failure Count: " + platformData.get("failureCount"));
                System.out.println("     Is Healthy: " + platformData.get("isHealthy"));
            }
        }

        assertNotNull(platformStats, "Platform statistics should be available");
    }

    @Test
    @DisplayName("5. Test Platform Health Monitoring")
    public void testPlatformHealthMonitoring() {
        System.out.println("\n=== TEST 5: Platform Health Monitoring ===");

        Map<String, Object> stats = scheduler.getSchedulerStatistics();

        @SuppressWarnings("unchecked")
        Map<String, Object> platformStats = (Map<String, Object>) stats.get("platforms");

        boolean allHealthy = true;

        for (String platform : new String[]{"SHOPEE", "TIKTOK", "FACEBOOK"}) {
            @SuppressWarnings("unchecked")
            Map<String, Object> platformData = (Map<String, Object>) platformStats.get(platform);

            if (platformData != null && Boolean.TRUE.equals(platformData.get("enabled"))) {
                Boolean isHealthy = (Boolean) platformData.get("isHealthy");
                if (!Boolean.TRUE.equals(isHealthy)) {
                    allHealthy = false;
                    System.out.println("⚠️ " + platform + " is not healthy");
                } else {
                    System.out.println("✅ " + platform + " is healthy");
                }
            }
        }

        if (allHealthy) {
            System.out.println("✅ All enabled platforms are healthy");
        }
    }

    @Test
    @DisplayName("6. Test Concurrent Execution Safety")
    public void testConcurrentExecutionSafety() {
        System.out.println("\n=== TEST 6: Concurrent Execution Safety ===");

        // Try to trigger multiple executions concurrently
        Thread thread1 = new Thread(() -> scheduler.triggerAllPlatforms());
        Thread thread2 = new Thread(() -> scheduler.triggerAllPlatforms());
        Thread thread3 = new Thread(() -> scheduler.triggerAllPlatforms());

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join(10000);
            thread2.join(10000);
            thread3.join(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> stats = scheduler.getSchedulerStatistics();
        Boolean isExecuting = (Boolean) stats.get("isCurrentlyExecuting");

        System.out.println("✅ Concurrent execution test completed");
        System.out.println("✅ Currently executing: " + isExecuting);

        // Should not be executing after threads complete
        assertFalse(Boolean.TRUE.equals(isExecuting), "Should not be executing after completion");
    }

    @Test
    @DisplayName("7. Test Platform-Specific Error Handling")
    public void testPlatformErrorHandling() {
        System.out.println("\n=== TEST 7: Platform Error Handling ===");

        // Test invalid platform trigger
        boolean invalidResult = scheduler.triggerPlatform("INVALID_PLATFORM");
        assertFalse(invalidResult, "Invalid platform should return false");
        System.out.println("✅ Invalid platform handled correctly");

        // Test disabled platform trigger
        boolean disabledResult = scheduler.triggerPlatform("DISABLED_PLATFORM");
        assertFalse(disabledResult, "Disabled platform should return false");
        System.out.println("✅ Disabled platform handled correctly");
    }

    @Test
    @DisplayName("8. Test Database Integration Across Platforms")
    public void testDatabaseIntegration() {
        System.out.println("\n=== TEST 8: Database Integration ===");

        long statusCountBefore = statusRepository.count();

        // Trigger all platforms to ensure status mappings are created
        scheduler.triggerAllPlatforms();

        // Wait for completion
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long statusCountAfter = statusRepository.count();

        System.out.println("✅ Status mappings: " + statusCountBefore + " → " + statusCountAfter);

        // Check platform-specific status mappings
        long shopeeStatuses = statusRepository.findByPlatformOrderByStatusKey("SHOPEE").size();
        long tiktokStatuses = statusRepository.findByPlatformOrderByStatusKey("TIKTOK").size();
        long facebookStatuses = statusRepository.findByPlatformOrderByStatusKey("FACEBOOK").size();

        System.out.println("✅ Platform status mappings:");
        System.out.println("   SHOPEE: " + shopeeStatuses);
        System.out.println("   TIKTOK: " + tiktokStatuses);
        System.out.println("   FACEBOOK: " + facebookStatuses);

        assertTrue(statusCountAfter >= statusCountBefore, "Status count should not decrease");
    }

    @Test
    @DisplayName("9. Performance Benchmark")
    public void performanceBenchmark() {
        System.out.println("\n=== TEST 9: Performance Benchmark ===");

        long startTime = System.currentTimeMillis();

        // Run multiple cycles to test performance
        for (int i = 0; i < 3; i++) {
            scheduler.triggerAllPlatforms();

            try {
                Thread.sleep(1000); // Small delay between cycles
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        System.out.println("✅ Performance Benchmark:");
        System.out.println("   Total time for 3 cycles: " + totalDuration + "ms");
        System.out.println("   Average per cycle: " + (totalDuration / 3) + "ms");

        // Performance assertion - should complete within reasonable time
        assertTrue(totalDuration < 60000, "3 cycles should complete within 60 seconds");

        Map<String, Object> finalStats = scheduler.getSchedulerStatistics();
        System.out.println("✅ Final execution count: " + finalStats.get("totalExecutions"));
    }
}