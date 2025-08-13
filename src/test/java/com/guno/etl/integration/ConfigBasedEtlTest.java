// ConfigBasedEtlTest_FacebookUpdate.java - Updated version with Facebook support
package com.guno.etl.integration;

import com.guno.etl.service.ShopeeEtlService;
import com.guno.etl.service.TikTokEtlService;
import com.guno.etl.service.FacebookEtlService;
import com.guno.etl.repository.OrderRepository;
import com.guno.etl.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@ComponentScan(basePackages = "com.guno.etl")
@Profile("test")
@PropertySource("classpath:test-config.properties")
public class ConfigBasedEtlTest implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedEtlTest.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Configuration values from test-config.properties
    @Value("${test.target.date:}")
    private String targetDateString;

    @Value("${test.platform:SHOPEE}")
    private String platform;

    @Value("${test.shopee.enabled:true}")
    private boolean shopeeEnabled;

    @Value("${test.tiktok.enabled:false}")
    private boolean tiktokEnabled;

    @Value("${test.facebook.enabled:false}")
    private boolean facebookEnabled;

    @Value("${test.mode:SINGLE_DATE}")
    private String testMode;

    @Value("${test.debug.enabled:false}")
    private boolean debugEnabled;

    @Value("${test.verbose.logging:false}")
    private boolean verboseLogging;

    // Services
    @Autowired(required = false)
    private ShopeeEtlService shopeeEtlService;

    @Autowired(required = false)
    private TikTokEtlService tiktokEtlService;

    @Autowired(required = false)
    private FacebookEtlService facebookEtlService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(ConfigBasedEtlTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        printBanner();
        loadConfiguration();
        runTest();
    }

    private void printBanner() {
        log.info("========================================");
        log.info("📁 CONFIG-BASED ETL TEST (Multi-Platform)");
        log.info("========================================");
        log.info("🔧 Edit: src/test/resources/test-config.properties");
        log.info("▶️  Run: ConfigBasedEtlTest from IDE");
        log.info("🏪 Supports: Shopee + TikTok + Facebook");
        log.info("========================================");
    }

    private void loadConfiguration() {
        log.info("📋 Loading Configuration...");

        // Parse target date
        LocalDate targetDate;
        try {
            if ("TODAY".equalsIgnoreCase(testMode)) {
                targetDate = LocalDate.now();
            } else if ("YESTERDAY".equalsIgnoreCase(testMode)) {
                targetDate = LocalDate.now().minusDays(1);
            } else if ("LAST_WEEK".equalsIgnoreCase(testMode)) {
                targetDate = LocalDate.now().minusDays(7);
            } else {
                targetDate = LocalDate.parse(targetDateString, DATE_FORMATTER);
            }
        } catch (Exception e) {
            log.warn("⚠️ Invalid date format, using today: {}", e.getMessage());
            targetDate = LocalDate.now();
        }

        // Display configuration
        log.info("📅 Target Date: {}", targetDate);
        log.info("🏪 Platform: {}", platform);
        log.info("🛒 Shopee Enabled: {}", shopeeEnabled);
        log.info("📱 TikTok Enabled: {}", tiktokEnabled);
        log.info("📘 Facebook Enabled: {}", facebookEnabled);
        log.info("🧪 Test Mode: {}", testMode);
        log.info("🐛 Debug Mode: {}", debugEnabled);

        // Store parsed date
        this.targetDateString = targetDate.format(DATE_FORMATTER);
    }

    private void runTest() {
        log.info("========================================");
        log.info("🚀 Starting Multi-Platform ETL Test");
        log.info("========================================");

        try {
            // Step 1: Database state before
            log.info("📊 STEP 1: Database State BEFORE");
            long ordersBefore = orderRepository.count();
            long customersBefore = customerRepository.count();

            log.info("   Orders: {}", ordersBefore);
            log.info("   Customers: {}", customersBefore);

            // Step 2: Process data
            log.info("🔄 STEP 2: Processing Data for {}", targetDateString);
            boolean success = processData();

            // Step 3: Database state after
            log.info("📊 STEP 3: Database State AFTER");
            long ordersAfter = orderRepository.count();
            long customersAfter = customerRepository.count();

            log.info("   Orders: {}", ordersAfter);
            log.info("   Customers: {}", customersAfter);

            // Step 4: Results
            showResults(ordersBefore, customersBefore, ordersAfter, customersAfter, success);

        } catch (Exception e) {
            log.error("❌ Test failed: {}", e.getMessage());
            if (debugEnabled) {
                e.printStackTrace();
            }
        }
    }

    private boolean processData() {
        boolean overallSuccess = true;
        int platformsProcessed = 0;

        // Process Shopee if enabled
        if (shouldProcessShopee()) {
            overallSuccess &= processShopeeData();
            platformsProcessed++;
        }

        // Process TikTok if enabled
        if (shouldProcessTikTok()) {
            overallSuccess &= processTikTokData();
            platformsProcessed++;
        }

        // Process Facebook if enabled
        if (shouldProcessFacebook()) {
            overallSuccess &= processFacebookData();
            platformsProcessed++;
        }

        if (platformsProcessed == 0) {
            log.warn("⚠️ No platforms enabled for processing!");
            log.warn("   💡 Check test-config.properties:");
            log.warn("   📝 test.shopee.enabled=true");
            log.warn("   📝 test.tiktok.enabled=true");
            log.warn("   📝 test.facebook.enabled=true");
            log.warn("   📝 test.platform=SHOPEE (or TIKTOK, FACEBOOK, ALL)");
            return false;
        }

        log.info("✅ Processed {} platform(s)", platformsProcessed);
        return overallSuccess;
    }

    private boolean shouldProcessShopee() {
        return shopeeEnabled &&
                ("SHOPEE".equalsIgnoreCase(platform) || "ALL".equalsIgnoreCase(platform)) &&
                shopeeEtlService != null;
    }

    private boolean shouldProcessTikTok() {
        return tiktokEnabled &&
                ("TIKTOK".equalsIgnoreCase(platform) || "ALL".equalsIgnoreCase(platform)) &&
                tiktokEtlService != null;
    }

    private boolean shouldProcessFacebook() {
        return facebookEnabled &&
                ("FACEBOOK".equalsIgnoreCase(platform) || "ALL".equalsIgnoreCase(platform)) &&
                facebookEtlService != null;
    }

    private boolean processShopeeData() {
        log.info("   🛒 Processing Shopee Data...");

        try {
            ShopeeEtlService.EtlResult result = shopeeEtlService.processOrdersForDate(targetDateString);

            if (verboseLogging) {
                log.info("     📥 API Response: {} total orders", result.getTotalOrders());
                log.info("     ✅ Processed: {} orders", result.getOrdersProcessed());
                log.info("     📈 Success Rate: {}%", result.getSuccessRate());
                log.info("     ⏱️ Duration: {} ms", result.getDurationMs());
            }

            log.info("     💾 Shopee Result: {}", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
            return result.isSuccess();

        } catch (Exception e) {
            log.error("     ❌ Shopee processing failed: {}", e.getMessage());
            if (debugEnabled) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean processTikTokData() {
        log.info("   📱 Processing TikTok Data...");

        try {
            TikTokEtlService.EtlResult result = tiktokEtlService.processOrdersForDate(targetDateString);

            if (verboseLogging) {
                log.info("     📥 API Response: {} total orders", result.getTotalOrders());
                log.info("     ✅ Processed: {} orders", result.getOrdersProcessed());
                log.info("     ⏱️ Duration: {} ms", result.getDurationMs());
            }

            log.info("     💾 TikTok Result: {}", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
            return result.isSuccess();

        } catch (Exception e) {
            log.error("     ❌ TikTok processing failed: {}", e.getMessage());
            if (debugEnabled) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean processFacebookData() {
        log.info("   📘 Processing Facebook Data...");

        try {
            FacebookEtlService.EtlResult result = facebookEtlService.processOrdersForDate(targetDateString);

            if (verboseLogging) {
                log.info("     📥 API Response: {} total orders", result.getTotalOrders());
                log.info("     ✅ Processed: {} orders", result.getOrdersProcessed());
                log.info("     📈 Success Rate: {}%", result.getSuccessRate());
                log.info("     ⏱️ Duration: {} ms", result.getDurationMs());
            }

            log.info("     💾 Facebook Result: {}", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
            return result.isSuccess();

        } catch (Exception e) {
            log.error("     ❌ Facebook processing failed: {}", e.getMessage());
            if (debugEnabled) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void showResults(long ordersBefore, long customersBefore,
                             long ordersAfter, long customersAfter, boolean success) {

        log.info("========================================");
        log.info("📈 MULTI-PLATFORM TEST RESULTS");
        log.info("========================================");

        long newOrders = ordersAfter - ordersBefore;
        long newCustomers = customersAfter - customersBefore;

        log.info("📅 Test Date: {}", targetDateString);
        log.info("🏪 Platform(s): {}", getPlatformSummary());
        log.info("📊 Database Changes:");
        log.info("   Orders: {} → {} (+{})", ordersBefore, ordersAfter, newOrders);
        log.info("   Customers: {} → {} (+{})", customersBefore, customersAfter, newCustomers);

        if (success && (newOrders > 0 || newCustomers > 0)) {
            log.info("✅ Result: 🎉 SUCCESS");
            log.info("   📥 {} new orders processed", newOrders);
            log.info("   👥 {} new customers added", newCustomers);
        } else if (success && newOrders == 0) {
            log.info("✅ Result: ⚠️ SUCCESS (No new data)");
            log.info("   💡 This may be normal - no orders for this date or all orders already exist");
        } else {
            log.info("❌ Result: FAILED");
            log.info("   🔧 Check configuration and logs above");
        }

        log.info("========================================");
        log.info("💡 To test different configurations:");
        log.info("   📝 Edit: src/test/resources/test-config.properties");
        log.info("   🛒 Shopee: test.shopee.enabled=true, test.platform=SHOPEE");
        log.info("   📱 TikTok: test.tiktok.enabled=true, test.platform=TIKTOK");
        log.info("   📘 Facebook: test.facebook.enabled=true, test.platform=FACEBOOK");
        log.info("   🌐 All: test.platform=ALL (enable all desired platforms)");
        log.info("   ▶️  Re-run: ConfigBasedEtlTest");
        log.info("========================================");
    }

    private String getPlatformSummary() {
        StringBuilder summary = new StringBuilder();

        if (shouldProcessShopee()) {
            summary.append("Shopee ");
        }
        if (shouldProcessTikTok()) {
            summary.append("TikTok ");
        }
        if (shouldProcessFacebook()) {
            summary.append("Facebook ");
        }

        String result = summary.toString().trim();
        return result.isEmpty() ? "None" : result;
    }
}