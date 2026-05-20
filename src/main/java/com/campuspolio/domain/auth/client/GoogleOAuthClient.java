package com.campuspolio.domain.auth.client;

import com.campuspolio.domain.auth.dto.GoogleUserInfo;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

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
        if (!StringUtils.hasText(idTokenString)) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (!StringUtils.hasText(googleClientId)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

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

            return new GoogleUserInfo(
                    googleId,
                    email,
                    emailVerified,
                    name,
                    picture
            );

        } catch (GeneralSecurityException e) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}