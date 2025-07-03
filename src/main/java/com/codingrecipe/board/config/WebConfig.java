package com.codingrecipe.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // [수정] application.yml의 'file-upload-path'와 이름을 정확히 일치시킵니다.
    @Value("${file-upload-path}")
    private String uploadPath;

    private String resourcePath = "/upload/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourcePath)
                .addResourceLocations("file:///" + uploadPath);
    }
}