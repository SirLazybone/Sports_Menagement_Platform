package com.course_work.Sports_Menagement_Platform.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get("").toAbsolutePath().normalize().toString() + "/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(0);
    }
} 