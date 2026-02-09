package com.mrs.mrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync 
public class MrsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MrsApplication.class, args);
    }
}