//package com.guno.etl.debug;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ComponentScan;
//
//@SpringBootApplication
//@ComponentScan(basePackages = "com.guno.etl")
//public class FacebookDebugTest implements CommandLineRunner {
//
//    @Autowired
//    private FacebookApiDebugTool debugTool;
//
//    public static void main(String[] args) {
//        System.setProperty("spring.profiles.active", "test");
//        SpringApplication.run(FacebookDebugTest.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        debugTool.debugFacebookApi();
//    }
//}
