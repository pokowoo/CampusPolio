package com.campuspolio.domain.auth.client;

import com.campuspolio.domain.auth.dto.GoogleUserInfo;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Component
public class GoogleOAuthClient {

    private final String googleClientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthClient(
            @Value("${google.oauth.client-id:}") String googleClientId
    ) {
        this.googleClientId = googleClientId;

        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleUserInfo getUserInfo(String idTokenString) {
        validateGoogleClientId();
        validateIdToken(idTokenString);

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (!StringUtils.hasText(googleId)) {
                throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }

            if (!StringUtils.hasText(email)) {
                throw new CustomException(ErrorCode.GOOGLE_EMAIL_NOT_FOUND);
            }

            return new GoogleUserInfo(
                    googleId,
                    email,
                    emailVerified,
                    name,
                    picture
            );

        } catch (CustomException e) {
            throw e;

        } catch (IllegalArgumentException e) {
            /*
             * GoogleIdTokenVerifier 내부에서 토큰 형식이 잘못된 경우
             * IllegalArgumentException이 발생할 수 있다.
             * 이 경우 서버 오류가 아니라 잘못된 토큰이므로 401로 변환한다.
             */
            log.warn("Invalid Google ID Token format", e);
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);

        } catch (GeneralSecurityException e) {
            log.warn("Google ID Token security verification failed", e);
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);

        } catch (IOException e) {
            /*
             * Google 공개키 조회 등 외부 통신 중 발생할 수 있는 예외
             */
            log.error("Google OAuth communication error", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateGoogleClientId() {
        if (!StringUtils.hasText(googleClientId)) {
            log.error("google.oauth.client-id is empty. Check application.yml or environment variables.");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateIdToken(String idTokenString) {
        if (!StringUtils.hasText(idTokenString)) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        /*
         * Google ID Token은 JWT 형식이며 일반적으로 header.payload.signature 구조다.
         * authorization code나 access token을 잘못 넣는 경우를 빠르게 차단한다.
         */
        if (idTokenString.split("\\.").length != 3) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }
}