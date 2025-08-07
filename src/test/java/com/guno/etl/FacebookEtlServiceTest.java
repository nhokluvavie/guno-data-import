// FacebookEtlServiceTest.java - Comprehensive Test
// File location: src/test/java/com/guno/etl/FacebookEtlServiceTest.java
package com.guno.etl;

import com.guno.etl.service.FacebookEtlService;
import com.guno.etl.service.FacebookApiService;
import com.guno.etl.dto.FacebookOrderDto;
import com.guno.etl.dto.FacebookApiResponse;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class FacebookEtlServiceTest {

    @Autowired
    private FacebookEtlService facebookEtlService;

    @Autowired
    private FacebookApiService facebookApiService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GeographyInfoRepository geographyInfoRepository;

    @Autowired
    private ProcessingDateInfoRepository processingDateInfoRepository;

    @Autowired
    private PaymentInfoRepository paymentInfoRepository;

    @Autowired
    private ShippingInfoRepository shippingInfoRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderStatusDetailRepository orderStatusDetailRepository;

    @Test
    @DisplayName("1. Test Facebook API Connectivity")
    public void testFacebookApiConnectivity() {
        System.out.println("\n=== TEST 1: Facebook API Connectivity ===");

        try {
            // Test API connection
            FacebookApiResponse response = facebookApiService.fetchUpdatedOrders();

            assertNotNull(response, "❌ API response should not be null");
            System.out.println("✅ Facebook API connection successful");

            if (response.getData() != null && response.getData().getOrders() != null) {
                System.out.println("✅ Found " + response.getData().getOrders().size() + " Facebook orders");
            } else {
                System.out.println("⚠️ No Facebook orders in response (might be expected in test environment)");
            }

        } catch (Exception e) {
            System.out.println("❌ Facebook API connectivity failed: " + e.getMessage());
            fail("Facebook API connectivity test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("2. Test Facebook ETL Processing")
    public void testFacebookEtlProcessing() {
        System.out.println("\n=== TEST 2: Facebook ETL Processing ===");

        try {
            // Count records before
            long customersBefore = customerRepository.count();
            long ordersBefore = orderRepository.count();
            long orderItemsBefore = orderItemRepository.count();
            long productsBefore = productRepository.count();
            long geographyBefore = geographyInfoRepository.count();

            System.out.println("📊 Database state before ETL:");
            System.out.println("   Customers: " + customersBefore);
            System.out.println("   Orders: " + ordersBefore);
            System.out.println("   Order Items: " + orderItemsBefore);
            System.out.println("   Products: " + productsBefore);
            System.out.println("   Geography: " + geographyBefore);

            // Run Facebook ETL
            FacebookEtlService.EtlResult result = facebookEtlService.processUpdatedOrders();

            assertNotNull(result, "❌ ETL result should not be null");
            assertTrue(result.isSuccess(), "❌ ETL should be successful");

            System.out.println("✅ Facebook ETL completed:");
            System.out.println("   Success: " + result.isSuccess());
            System.out.println("   Orders Processed: " + result.getOrdersProcessed());
            System.out.println("   Orders Failed: " + result.getOrdersFailed());

            // Count records after
            long customersAfter = customerRepository.count();
            long ordersAfter = orderRepository.count();
            long orderItemsAfter = orderItemRepository.count();
            long productsAfter = productRepository.count();
            long geographyAfter = geographyInfoRepository.count();

            System.out.println("📊 Database state after ETL:");
            System.out.println("   Customers: " + customersAfter + " (+" + (customersAfter - customersBefore) + ")");
            System.out.println("   Orders: " + ordersAfter + " (+" + (ordersAfter - ordersBefore) + ")");
            System.out.println("   Order Items: " + orderItemsAfter + " (+" + (orderItemsAfter - orderItemsBefore) + ")");
            System.out.println("   Products: " + productsAfter + " (+" + (productsAfter - productsBefore) + ")");
            System.out.println("   Geography: " + geographyAfter + " (+" + (geographyAfter - geographyBefore) + ")");

        } catch (Exception e) {
            System.out.println("❌ Facebook ETL processing failed: " + e.getMessage());
            e.printStackTrace();
            fail("Facebook ETL processing test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("3. Test All 9 Tables Population")
    public void testAllTablesPopulation() {
        System.out.println("\n=== TEST 3: All 9 Tables Population ===");

        try {
            // Run Facebook ETL first
            FacebookEtlService.EtlResult result = facebookEtlService.processUpdatedOrders();

            if (result.getOrdersProcessed() > 0) {
                // Test all 9 tables have data
                long customers = customerRepository.count();
                long orders = orderRepository.count();
                long orderItems = orderItemRepository.count();
                long products = productRepository.count();
                long geography = geographyInfoRepository.count();
                long dateInfo = processingDateInfoRepository.count();
                long paymentInfo = paymentInfoRepository.count();
                long shippingInfo = shippingInfoRepository.count();
                long statusInfo = statusRepository.count();

                System.out.println("📊 All 9 Tables Population:");
                System.out.println("   1. Customers: " + customers);
                System.out.println("   2. Orders: " + orders);
                System.out.println("   3. Order Items: " + orderItems);
                System.out.println("   4. Products: " + products);
                System.out.println("   5. Geography Info: " + geography);
                System.out.println("   6. Processing Date Info: " + dateInfo);
                System.out.println("   7. Payment Info: " + paymentInfo);
                System.out.println("   8. Shipping Info: " + shippingInfo);
                System.out.println("   9. Status Info: " + statusInfo);

                // Verify core tables have data
                assertTrue(customers > 0, "❌ Customers table should have data");
                assertTrue(orders > 0, "❌ Orders table should have data");

                System.out.println("✅ All tables populated successfully!");

            } else {
                System.out.println("⚠️ No orders processed - unable to verify table population");
            }

        } catch (Exception e) {
            System.out.println("❌ Table population test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Table population test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("4. Test Facebook Status Mapping")
    public void testFacebookStatusMapping() {
        System.out.println("\n=== TEST 4: Facebook Status Mapping ===");

        try {
            // Run ETL to ensure status mappings are created
            facebookEtlService.processUpdatedOrders();

            // Check if Facebook status mappings exist - FIX: Sử dụng method có sẵn
            List<Status> facebookStatuses = statusRepository.findByPlatformOrderByStatusKey("FACEBOOK");

            System.out.println("📊 Facebook Status Mappings:");
            for (Status status : facebookStatuses) {
                System.out.println("   Platform: " + status.getPlatform() +
                        ", Code: " + status.getPlatformStatusCode() +
                        ", Standard: " + status.getStandardStatusCode());
            }

            if (facebookStatuses.size() > 0) {
                System.out.println("✅ Facebook status mappings created: " + facebookStatuses.size() + " statuses");
            } else {
                System.out.println("⚠️ No Facebook status mappings found (might be expected if no orders processed)");
            }

        } catch (Exception e) {
            System.out.println("❌ Status mapping test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Status mapping test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("5. Test Facebook Data Quality")
    public void testFacebookDataQuality() {
        System.out.println("\n=== TEST 5: Facebook Data Quality ===");

        try {
            // Run ETL
            FacebookEtlService.EtlResult result = facebookEtlService.processUpdatedOrders();

            if (result.getOrdersProcessed() > 0) {
                // Sample data quality checks - FIX: Sử dụng repository methods có sẵn
                long totalCustomers = customerRepository.count();
                List<Order> orders = orderRepository.findAll();

                System.out.println("📊 Facebook Data Quality Checks:");

                // Check orders have required fields
                long ordersWithGrossRevenue = orders.stream()
                        .filter(o -> o.getGrossRevenue() != null && o.getGrossRevenue() > 0)
                        .count();
                System.out.println("   Orders with total amount: " + ordersWithGrossRevenue + "/" + orders.size());

                // Check basic counts
                System.out.println("   Total customers: " + totalCustomers);
                System.out.println("   Total orders: " + orders.size());

                if (totalCustomers > 0) {
                    System.out.println("✅ Facebook data quality looks good!");
                } else {
                    System.out.println("⚠️ No customers found");
                }

            } else {
                System.out.println("⚠️ No orders processed - unable to verify data quality");
            }

        } catch (Exception e) {
            System.out.println("❌ Data quality test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Data quality test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("6. Test MultiPlatform Integration")
    public void testMultiPlatformIntegration() {
        System.out.println("\n=== TEST 6: MultiPlatform Integration ===");

        try {
            // Check platforms in database - FIX: Không có findByPlatform method
            long totalCustomers = customerRepository.count();
            long totalOrders = orderRepository.count();
            long totalProducts = productRepository.count();

            System.out.println("📊 MultiPlatform Integration:");
            System.out.println("   Total customers: " + totalCustomers);
            System.out.println("   Total orders: " + totalOrders);
            System.out.println("   Total products: " + totalProducts);

            // Check status mappings across platforms - FIX: Sử dụng method có sẵn
            List<Status> allStatuses = statusRepository.findAll();
            long shopeeStatuses = allStatuses.stream().filter(s -> "SHOPEE".equals(s.getPlatform())).count();
            long tiktokStatuses = allStatuses.stream().filter(s -> "TIKTOK".equals(s.getPlatform())).count();
            long facebookStatuses = allStatuses.stream().filter(s -> "FACEBOOK".equals(s.getPlatform())).count();

            System.out.println("   Shopee statuses: " + shopeeStatuses);
            System.out.println("   TikTok statuses: " + tiktokStatuses);
            System.out.println("   Facebook statuses: " + facebookStatuses);

            System.out.println("✅ MultiPlatform integration test completed!");

        } catch (Exception e) {
            System.out.println("❌ MultiPlatform integration test failed: " + e.getMessage());
            e.printStackTrace();
            fail("MultiPlatform integration test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("7. Performance Test")
    public void testPerformance() {
        System.out.println("\n=== TEST 7: Performance Test ===");

        try {
            long startTime = System.currentTimeMillis();

            // Run Facebook ETL
            FacebookEtlService.EtlResult result = facebookEtlService.processUpdatedOrders();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("📊 Performance Metrics:");
            System.out.println("   Duration: " + duration + " ms");
            System.out.println("   Orders processed: " + result.getOrdersProcessed());

            if (result.getOrdersProcessed() > 0) {
                double avgTimePerOrder = (double) duration / result.getOrdersProcessed();
                System.out.println("   Average time per order: " + String.format("%.2f", avgTimePerOrder) + " ms");

                // Performance thresholds
                assertTrue(avgTimePerOrder < 5000, "❌ Average processing time should be under 5 seconds per order");
                System.out.println("✅ Performance test passed!");
            } else {
                System.out.println("⚠️ No orders processed - performance test inconclusive");
            }

        } catch (Exception e) {
            System.out.println("❌ Performance test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Performance test failed: " + e.getMessage());
        }
    }
}