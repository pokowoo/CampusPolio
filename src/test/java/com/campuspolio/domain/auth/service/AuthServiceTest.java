package com.campuspolio.domain.auth.service;

import com.campuspolio.domain.auth.client.GoogleOAuthClient;
import com.campuspolio.domain.auth.dto.GoogleUserInfo;
import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private GoogleOAuthClient googleOAuthClient;
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        googleOAuthClient = mock(GoogleOAuthClient.class);
        userRepository = mock(UserRepository.class);
        authService = new AuthService(googleOAuthClient, userRepository);
    }

    @Test
    @DisplayName("신규 Google 사용자가 로그인하면 유저를 생성하고 응답을 반환한다")
    void login_newUser_createsUser() throws Exception {
        String accessToken = "fake-token";
        String googleId = "google-sub-123";
        String email = "user@syu.ac.kr";

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        "https://image.com/profile.png"
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    setId(user, 1L);
                    return user;
                });

        LoginResponse response = authService.login(new LoginRequest(accessToken));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.isDomainValid()).isTrue();
        assertThat(response.isVerified()).isFalse();

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("기존 사용자가 로그인하면 새 유저를 생성하지 않는다")
    void login_existingUser_doesNotCreateUser() throws Exception {
        String accessToken = "fake-token";
        String googleId = "google-sub-123";
        String email = "user@syu.ac.kr";

        User existingUser = User.createGoogleUser(email, googleId, true);
        setId(existingUser, 1L);

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        null
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.of(existingUser));

        LoginResponse response = authService.login(new LoginRequest(accessToken));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.isDomainValid()).isTrue();

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("@syu.ac.kr 이메일이면 domainValid가 true다")
    void login_syuEmail_domainValidTrue() throws Exception {
        String accessToken = "fake-token";
        String googleId = "google-sub-123";
        String email = "user@syu.ac.kr";

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        null
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    setId(user, 1L);
                    return user;
                });

        LoginResponse response = authService.login(new LoginRequest(accessToken));

        assertThat(response.isDomainValid()).isTrue();
    }

    @Test
    @DisplayName("@syu.ac.kr 이메일이 아니면 domainValid가 false다")
    void login_nonSyuEmail_domainValidFalse() throws Exception {
        String accessToken = "fake-token";
        String googleId = "google-sub-456";
        String email = "user@gmail.com";

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        null
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    setId(user, 2L);
                    return user;
                });

        LoginResponse response = authService.login(new LoginRequest(accessToken));

        assertThat(response.isDomainValid()).isFalse();
    }

    @Test
    @DisplayName("Google 이메일이 없으면 예외가 발생한다")
    void login_withoutEmail_throwsException() {
        String accessToken = "fake-token";

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        "google-sub-123",
                        null,
                        true,
                        "홍길동",
                        null
                ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(accessToken)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GOOGLE_EMAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Google sub가 없으면 예외가 발생한다")
    void login_withoutGoogleId_throwsException() {
        String accessToken = "fake-token";

        when(googleOAuthClient.getUserInfo(accessToken))
                .thenReturn(new GoogleUserInfo(
                        null,
                        "user@syu.ac.kr",
                        true,
                        "홍길동",
                        null
                ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(accessToken)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_GOOGLE_TOKEN.getMessage());
    }

    private void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }
}