//package com.guno.etl.integration;
//
//import com.guno.etl.service.TikTokEtlService;
//import com.guno.etl.service.TikTokApiService;
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
//public class TikTokOnlyTest implements CommandLineRunner {
//
//    private static final Logger log = LoggerFactory.getLogger(TikTokOnlyTest.class);
//
//    @Autowired
//    private TikTokEtlService tiktokEtlService;
//
//    @Autowired
//    private TikTokApiService tiktokApiService;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    public static void main(String[] args) {
//        System.setProperty("spring.profiles.active", "test");
//        System.setProperty("etl.platforms.shopee.enabled", "false");
//        System.setProperty("etl.platforms.tiktok.enabled", "true");
//        SpringApplication.run(TikTokOnlyTest.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("========================================");
//        log.info("📱 TIKTOK-ONLY ETL TEST");
//        log.info("========================================");
//
//        // Test 1: TikTok API Connectivity
//        testTikTokApiConnectivity();
//
//        // Test 2: Database State Before
//        long ordersBefore = orderRepository.count();
//        long customersBefore = customerRepository.count();
//        log.info("📊 Database Before: {} orders, {} customers", ordersBefore, customersBefore);
//
//        // Test 3: TikTok ETL Processing
//        testTikTokEtlProcessing();
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
//        log.info("🎉 TIKTOK-ONLY TEST COMPLETED!");
//        log.info("========================================");
//    }
//
//    private void testTikTokApiConnectivity() {
//        log.info("📡 Testing TikTok API Connectivity...");
//        try {
//            boolean healthy = tiktokApiService.isApiHealthy();
//            log.info("   TikTok API Health: {}", healthy ? "✅ Connected" : "❌ Failed");
//        } catch (Exception e) {
//            log.error("   TikTok API Error: {}", e.getMessage());
//        }
//    }
//
//    private void testTikTokEtlProcessing() {
//        log.info("🔄 Testing TikTok ETL Processing...");
//        try {
//            TikTokEtlService.EtlResult result = tiktokEtlService.processUpdatedOrders();
//
//            log.info("   Orders Processed: {}/{}", result.getOrdersProcessed(), result.getTotalOrders());
//            log.info("   Duration: {} ms", result.getDurationMs());
//            log.info("   Result: {}", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
//
//        } catch (Exception e) {
//            log.error("   ETL Processing Error: {}", e.getMessage());
//        }
//    }
//}