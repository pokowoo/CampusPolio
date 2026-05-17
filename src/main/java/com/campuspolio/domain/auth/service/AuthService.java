package com.campuspolio.domain.auth.service;

import com.campuspolio.domain.auth.client.GoogleOAuthClient;
import com.campuspolio.domain.auth.dto.GoogleUserInfo;
import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String SCHOOL_EMAIL_DOMAIN = "@syu.ac.kr";

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(request.accessToken());

        String googleId = googleUserInfo.sub();
        String email = googleUserInfo.email();

        if (googleId == null || googleId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_EMAIL_NOT_FOUND);
        }

        boolean isDomainValid = email.endsWith(SCHOOL_EMAIL_DOMAIN);

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createUser(email, googleId, isDomainValid));

        user.updateDomainValid(isDomainValid);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.isDomainValid(),
                user.isVerified()
        );
    }

    private User createUser(String email, String googleId, boolean isDomainValid) {
        User user = User.createGoogleUser(email, googleId, isDomainValid);
        return userRepository.save(user);
    }
}