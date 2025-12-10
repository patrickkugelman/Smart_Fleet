package com.smartfleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Smart Fleet Tracking System
 * CRITICAL: @EnableAsync is required for asynchronous email sending
 */
@SpringBootApplication
@EnableAsync
public class SmartFleetTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFleetTrackingApplication.class, args);
        System.out.println("Smart Fleet Tracking System started successfully!");
    }

}
