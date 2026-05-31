package com.project.software_engineering.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 런타임에 업로드된 이미지를 즉시 서빙할 수 있도록 실제 물리적 경로를 매핑
        String projectPath = System.getProperty("user.dir");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + projectPath + "/src/main/resources/static/images/");
    }
}
