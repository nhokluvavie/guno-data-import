// SchedulingConfig.java - Configuration for ETL Scheduler
package com.guno.etl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    @Value("${etl.scheduler.thread-pool-size:2}")
    private int threadPoolSize;

    @Value("${etl.scheduler.thread-name-prefix:ETL-Scheduler-}")
    private String threadNamePrefix;

    @Value("${etl.scheduler.await-termination-seconds:20}")
    private int awaitTerminationSeconds;

    /**
     * Configure task scheduler for ETL operations
     */
    @Bean(name = "etlTaskScheduler")
    public TaskScheduler etlTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Basic configuration
        scheduler.setPoolSize(threadPoolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // Rejection policy - what to do when thread pool is full
        scheduler.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());

        // Thread configuration
        scheduler.setDaemon(false); // Ensure threads don't prevent JVM shutdown
        scheduler.setRemoveOnCancelPolicy(true); // Remove cancelled tasks

        // Initialize the scheduler
        scheduler.initialize();

        log.info("ETL Task Scheduler configured:");
        log.info("  Thread Pool Size: {}", threadPoolSize);
        log.info("  Thread Name Prefix: {}", threadNamePrefix);
        log.info("  Await Termination: {} seconds", awaitTerminationSeconds);

        return scheduler;
    }

    /**
     * Custom rejection handler for when thread pool is full
     */
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        private static final Logger log = LoggerFactory.getLogger(CustomRejectedExecutionHandler.class);

        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            log.error("ETL Task rejected - Thread pool is full!");
            log.error("Pool Size: {}, Active: {}, Queue Size: {}",
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());

            // For ETL, we want to log the rejection but not throw exception
            // This prevents scheduler from stopping due to thread pool issues
            log.warn("ETL task will be skipped due to thread pool saturation");
        }
    }
}