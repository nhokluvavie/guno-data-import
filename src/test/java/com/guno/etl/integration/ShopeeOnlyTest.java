//package com.guno.etl.integration;
//
//import com.guno.etl.service.ShopeeEtlService;
//import com.guno.etl.service.ShopeeApiService;
//import com.guno.etl.repository.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Profile;
//
//@SpringBootApplication
//@ComponentScan(basePackages = "com.guno.etl")
//@Profile("test")
//public class ShopeeOnlyTest implements CommandLineRunner {
//
//    private static final Logger log = LoggerFactory.getLogger(ShopeeOnlyTest.class);
//
//    @Autowired
//    private ShopeeEtlService shopeeEtlService;
//
//    @Autowired
//    private ShopeeApiService shopeeApiService;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    public static void main(String[] args) {
//        System.setProperty("spring.profiles.active", "test");
//        System.setProperty("etl.platforms.shopee.enabled", "true");
//        System.setProperty("etl.platforms.tiktok.enabled", "false");
//        SpringApplication.run(ShopeeOnlyTest.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("========================================");
//        log.info("🛒 SHOPEE-ONLY ETL TEST");
//        log.info("========================================");
//
//        // Test 1: Shopee API Connectivity
//        testShopeeApiConnectivity();
//
//        // Test 2: Database State Before
//        long ordersBefore = orderRepository.count();
//        long customersBefore = customerRepository.count();
//        log.info("📊 Database Before: {} orders, {} customers", ordersBefore, customersBefore);
//
//        // Test 3: Shopee ETL Processing
//        testShopeeEtlProcessing();
//
//        // Test 4: Database State After
//        long ordersAfter = orderRepository.count();
//        long customersAfter = customerRepository.count();
//        log.info("📊 Database After: {} orders, {} customers", ordersAfter, customersAfter);
//
//        // Test 5: Results
//        long newOrders = ordersAfter - ordersBefore;
//        long newCustomers = customersAfter - customersBefore;
//        log.info("📈 New Data: {} orders, {} customers", newOrders, newCustomers);
//
//        log.info("========================================");
//        log.info("🎉 SHOPEE-ONLY TEST COMPLETED!");
//        log.info("========================================");
//    }
//
//    private void testShopeeApiConnectivity() {
//        log.info("📡 Testing Shopee API Connectivity...");
//        try {
//            boolean healthy = shopeeApiService.isApiHealthy();
//            log.info("   Shopee API Health: {}", healthy ? "✅ Connected" : "❌ Failed");
//        } catch (Exception e) {
//            log.error("   Shopee API Error: {}", e.getMessage());
//        }
//    }
//
//    private void testShopeeEtlProcessing() {
//        log.info("🔄 Testing Shopee ETL Processing...");
//        try {
//            ShopeeEtlService.EtlResult result = shopeeEtlService.processUpdatedOrders();
//
//            log.info("   Orders Processed: {}/{}", result.getOrdersProcessed(), result.getTotalOrders());
//            log.info("   Success Rate: {}%", result.getSuccessRate());
//            log.info("   Duration: {} ms", result.getDurationMs());
//            log.info("   Result: {}", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
//
//        } catch (Exception e) {
//            log.error("   ETL Processing Error: {}", e.getMessage());
//        }
//    }
//}