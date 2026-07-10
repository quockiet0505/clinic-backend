package com.clinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình Thread Pool dành riêng cho các tác vụ AI Moderation chạy nền (@Async).
 * Tránh block luồng xử lý HTTP chính khi gọi AI Server kiểm duyệt bình luận.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "aiModerationExecutor")
    public Executor aiModerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-moderation-");
        executor.initialize();
        return executor;
    }
}
