package com.campuspolio.global.claude;

import com.campuspolio.global.claude.dto.ClaudeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClaudeClient {

    private final RestClient claudeRestClient;
    private final ObjectMapper objectMapper;

    @Value("${claude.api-key}")
    private String apiKey;

    public String review(
            String prompt
    ) {

        try {

            String response =
                    claudeRestClient.post()
                            .uri("/v1/messages")
                            .header("x-api-key", apiKey)
                            .header("anthropic-version", "2023-06-01")
                            .body(
                                    Map.of(
                                            "model",
                                            "claude-sonnet-4-6",
                                            "max_tokens",
                                            3000,
                                            "messages",
                                            new Object[]{
                                                    Map.of(
                                                            "role",
                                                            "user",
                                                            "content",
                                                            prompt
                                                    )
                                            }
                                    )
                            )
                            .retrieve()
                            .body(String.class);

            ClaudeResponse claudeResponse =
                    objectMapper.readValue(
                            response,
                            ClaudeResponse.class
                    );

            return claudeResponse.content()
                    .get(0)
                    .text();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Claude 리뷰 생성 실패",
                    e
            );
        }
    }
}