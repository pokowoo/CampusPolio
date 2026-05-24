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

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {

        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(request.idToken());

        String googleId = googleUserInfo.sub();
        String email = googleUserInfo.email();

        validateGoogleInfo(googleId, email);

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createUser(email, googleId));

        handleDeletedUser(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.isUniversityVerified()
        );
    }

    private void validateGoogleInfo(String googleId, String email) {
        if (googleId == null || googleId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_EMAIL_NOT_FOUND);
        }
    }

    private User createUser(String email, String googleId) {
        return userRepository.save(User.createGoogleUser(email, googleId));
    }

    private void handleDeletedUser(User user) {
        if (user.isDeleted()) {
            user.restore();
        }
    }
}