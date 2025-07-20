package com.codingrecipe.tip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.codingrecipe.tip")
public class TipApplication {
    public static void main(String[] args) {
        SpringApplication.run(TipApplication.class, args);
    }
}

