package com.campuspolio.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI campusPolioOpenAPI() {

        return new OpenAPI()

                .addServersItem(
                        new Server()
                                .url("https://api.campuspolio.cloud")
                                .description("Production Server")
                )

                .info(
                        new Info()
                                .title("CampusPolio API")
                                .description("CampusPolio 백엔드 API 명세서")
                                .version("v1.0.0")
                );
    }
}