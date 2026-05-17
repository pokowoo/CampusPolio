package com.campuspolio.domain.auth.client;

import com.campuspolio.domain.auth.dto.GoogleUserInfo;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleOAuthClient {

    private final RestClient restClient;

    public GoogleOAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            GoogleUserInfo userInfo = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleUserInfo.class);

            if (userInfo == null) {
                throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }

            return userInfo;
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }
}