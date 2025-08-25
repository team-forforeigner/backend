package com.codingrecipe.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 스프링의 비동기 기능 활성화
public class AsyncConfig {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 기본 스레드 수
        executor.setCorePoolSize(7);
        // 최대 스레드 수
        executor.setMaxPoolSize(12);
        // 큐 용량
        executor.setQueueCapacity(100);
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}