package com.clinic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from public directory
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:public/images/");
        registry.addResourceHandler("/icons/**")
                .addResourceLocations("file:public/icons/");
    }
}