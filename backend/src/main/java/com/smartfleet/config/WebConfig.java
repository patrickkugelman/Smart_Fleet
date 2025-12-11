package com.smartfleet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Această configurare spune Spring Boot:
        // "Când cineva cere un URL care începe cu /uploads/..."
        // "... caută fișierul real în folderul 'uploads/' de pe disc."
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}