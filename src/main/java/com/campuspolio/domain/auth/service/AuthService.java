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

    private static final String UNIVERSITY_EMAIL_DOMAIN_SUFFIX = ".ac.kr";

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(request.idToken());

        String googleId = googleUserInfo.sub();
        String email = googleUserInfo.email();

        if (googleId == null || googleId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_EMAIL_NOT_FOUND);
        }

        boolean isDomainValid = isUniversityEmail(email);

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createUser(email, googleId, isDomainValid));

        if (user.isDeleted()) {
            user.restore();
        }

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

    private boolean isUniversityEmail(String email) {
        int atIndex = email.indexOf("@");

        if (atIndex == -1 || atIndex == email.length() - 1) {
            return false;
        }

        String domain = email.substring(atIndex + 1).toLowerCase();

        return domain.endsWith(UNIVERSITY_EMAIL_DOMAIN_SUFFIX);
    }
}