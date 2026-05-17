package com.campuspolio.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔥 CORS 활성화 (Security에서 반드시 연결)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 🔥 CSRF 비활성화 (REST API 필수)
                .csrf(csrf -> csrf.disable())

                // 🔥 OPTIONS preflight 허용 (중요 ⭐)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 🔥 허용 Origin (로컬 + 테스트용 file:// 대응)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5500",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5500",
                "null"

        ));

        // 🔥 HTTP 메서드 허용 (preflight 대응)
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // 🔥 모든 헤더 허용 (Authorization 포함)
        config.setAllowedHeaders(List.of("*"));

        // 🔥 쿠키/세션 허용 (credentials true 대응)
        config.setAllowCredentials(true);

        // 🔥 프론트에서 헤더 읽기 가능하게 (JWT용)
        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}