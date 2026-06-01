package com.campuspolio.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI campusPolioOpenAPI() {

        OpenAPI openAPI = new OpenAPI();

        openAPI.setServers(
                List.of(
                        new Server()
                                .url("https://api.campuspolio.cloud")
                                .description("Production Server")
                )
        );

        openAPI.setInfo(
                new Info()
                        .title("CampusPolio API")
                        .description("CampusPolio 백엔드 API 명세서")
                        .version("v1.0.0")
        );

        return openAPI;
    }
}