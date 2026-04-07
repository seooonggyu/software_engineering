package com.project.software_engineering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsFilterConfiguration {
   @Bean
   public CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.setAllowedOriginPatterns(Arrays.asList("*"));
      config.setAllowedHeaders(Arrays.asList("Authorization", "RefreshToken", "Content-Type", "Accept", "Origin", "X-Requested-With"));
      config.addAllowedMethod("*");
      config.setExposedHeaders(Arrays.asList("Authorization", "RefreshToken"));
      source.registerCorsConfiguration("/api/**", config);
      return new CorsFilter(source);
   }
}