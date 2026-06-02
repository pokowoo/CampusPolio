package com.campuspolio.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClaudeConfig {

    @Bean
    public RestClient claudeRestClient() {

        return RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .build();
    }
}