package com.survival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages =  "com.survival")
@EntityScan(basePackages = "com.survival.Entity")
@EnableJpaRepositories(basePackages = "com.survival.repository")
@SpringBootApplication
public class SurvivalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurvivalApplication.class, args);
    }

}
