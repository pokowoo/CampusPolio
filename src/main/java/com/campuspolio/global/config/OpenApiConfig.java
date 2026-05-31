package com.campuspolio.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/*
 로컬용으로 설정해서 수정해야할 가능성 매우높음
 */

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Server"
                )
        }
)
public class OpenApiConfig {
}