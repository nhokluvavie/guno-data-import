// ShopeeEtlApplication.java - Final Version with Startup Handling
package com.guno.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class ShopeeEtlApplication {

    private static final Logger log = LoggerFactory.getLogger(ShopeeEtlApplication.class);

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        try {
            System.setProperty("spring.banner.location", "classpath:banner.txt");

            log.info("Starting Shopee ETL Application...");

            SpringApplication app = new SpringApplication(ShopeeEtlApplication.class);

            // Configure application
            app.setLogStartupInfo(true);
            app.setRegisterShutdownHook(true);

            // Start application
            app.run(args);

        } catch (Exception e) {
            log.error("Failed to start Shopee ETL Application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            logApplicationInfo();
            logConfigurationInfo();
            logAccessInfo();

        } catch (Exception e) {
            log.error("Error during application ready event: {}", e.getMessage(), e);
        }
    }

    private void logApplicationInfo() {
        log.info("=".repeat(60));
        log.info("🎉 SHOPEE ETL APPLICATION STARTED SUCCESSFULLY 🎉");
        log.info("=".repeat(60));

        // Application info
        String appName = env.getProperty("spring.application.name", "shopee-etl");
        String profile = String.join(",", env.getActiveProfiles());
        if (profile.isEmpty()) {
            profile = "default";
        }

        log.info("Application: {}", appName);
        log.info("Profile: {}", profile);
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Spring Boot Version: {}", org.springframework.boot.SpringBootVersion.getVersion());
    }

    private void logConfigurationInfo() {
        log.info("");
        log.info("📋 Configuration Summary:");
        log.info("-".repeat(40));

        // Database configuration
        String dbUrl = env.getProperty("spring.datasource.url", "Not configured");
        String dbUsername = env.getProperty("spring.datasource.username", "Not configured");
        log.info("Database URL: {}", dbUrl);
        log.info("Database User: {}", dbUsername);

        // ETL API configuration
        String apiUrl = env.getProperty("etl.api.base-url", "Not configured");
        String apiTimeout = env.getProperty("etl.api.timeout", "Not configured");
        String apiPageSize = env.getProperty("etl.api.page-size", "Not configured");
        log.info("API URL: {}", apiUrl);
        log.info("API Timeout: {}", apiTimeout);
        log.info("API Page Size: {}", apiPageSize);

        // Scheduler configuration
        String schedulerEnabled = env.getProperty("etl.scheduler.enabled", "Not configured");
        String schedulerRate = env.getProperty("etl.scheduler.fixed-rate", "Not configured");
        log.info("Scheduler Enabled: {}", schedulerEnabled);
        log.info("Scheduler Rate: {} ms", schedulerRate);

        // Logging configuration
        String logLevel = env.getProperty("logging.level.com.guno.etl", "Not configured");
        String logFile = env.getProperty("logging.file.name", "Not configured");
        log.info("Log Level: {}", logLevel);
        log.info("Log File: {}", logFile);
    }

    private void logAccessInfo() {
        try {
            log.info("");
            log.info("🌐 Access Information:");
            log.info("-".repeat(40));

            String port = env.getProperty("server.port", "8080");
            String host = InetAddress.getLocalHost().getHostAddress();

            log.info("Server Port: {}", port);
            log.info("Local Access: http://localhost:{}", port);
            log.info("Network Access: http://{}:{}", host, port);
            log.info("");
            log.info("📚 API Documentation:");
            log.info("Help: http://localhost:{}/api/etl/help", port);
            log.info("Status: http://localhost:{}/api/etl/status", port);
            log.info("Test API: http://localhost:{}/api/etl/api-test", port);
            log.info("Manual ETL: POST http://localhost:{}/api/etl/trigger", port);
            log.info("");
            log.info("🔧 Management Endpoints:");
            log.info("Health Check: http://localhost:{}/actuator/health", port);
            log.info("Application Info: http://localhost:{}/actuator/info", port);
            log.info("");
            log.info("⚡ ETL Operations:");
            boolean schedulerEnabled = Boolean.parseBoolean(env.getProperty("etl.scheduler.enabled", "false"));
            if (schedulerEnabled) {
                String fixedRate = env.getProperty("etl.scheduler.fixed-rate", "30000");
                int intervalSeconds = Integer.parseInt(fixedRate) / 1000;
                log.info("✅ Automated ETL: ENABLED (every {} seconds)", intervalSeconds);
                log.info("✅ Processing updated orders automatically");
            } else {
                log.info("⚠️ Automated ETL: DISABLED");
                log.info("💡 Use POST /api/etl/trigger for manual processing");
            }

        } catch (UnknownHostException e) {
            log.warn("Could not determine network address: {}", e.getMessage());
            String port = env.getProperty("server.port", "8080");
            log.info("Local Access: http://localhost:{}", port);
        }

        log.info("");
        log.info("=".repeat(60));
        log.info("🚀 Ready to process Shopee orders!");
        log.info("=".repeat(60));
    }
}