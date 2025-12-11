package com.smartfleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling // <-- ACEASTA ESTE LINIA NOUĂ CRITICĂ
public class SmartFleetTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFleetTrackingApplication.class, args);
        System.out.println("🚗 Smart Fleet System is RUNNING with Simulation Engine!");
    }
}