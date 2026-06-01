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
        String idToken = "fake-id-token";
        String googleId = "google-sub-123";
        String email = "user@korea.ac.kr";

        when(googleOAuthClient.getUserInfo(idToken))
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

        LoginResponse response = authService.login(new LoginRequest(idToken));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.universityVerified()).isFalse();

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("기존 사용자가 로그인하면 새 유저를 생성하지 않는다")
    void login_existingUser_doesNotCreateUser() throws Exception {
        String idToken = "fake-id-token";
        String googleId = "google-sub-123";
        String email = "user@korea.ac.kr";

        User existingUser = User.createGoogleUser(email, googleId);
        setId(existingUser, 1L);

        when(googleOAuthClient.getUserInfo(idToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        null
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.of(existingUser));

        LoginResponse response = authService.login(new LoginRequest(idToken));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.universityVerified()).isFalse();

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Google 이메일이 없으면 예외가 발생한다")
    void login_withoutEmail_throwsException() {
        String idToken = "fake-id-token";

        when(googleOAuthClient.getUserInfo(idToken))
                .thenReturn(new GoogleUserInfo(
                        "google-sub-123",
                        null,
                        true,
                        "홍길동",
                        null
                ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(idToken)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GOOGLE_EMAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Google sub가 없으면 예외가 발생한다")
    void login_withoutGoogleId_throwsException() {
        String idToken = "fake-id-token";

        when(googleOAuthClient.getUserInfo(idToken))
                .thenReturn(new GoogleUserInfo(
                        null,
                        "user@korea.ac.kr",
                        true,
                        "홍길동",
                        null
                ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(idToken)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_GOOGLE_TOKEN.getMessage());
    }

    @Test
    @DisplayName("잘못된 Google idToken이면 예외가 발생한다")
    void login_invalidGoogleIdToken_throwsException() {
        String idToken = "invalid-id-token";

        when(googleOAuthClient.getUserInfo(idToken))
                .thenThrow(new CustomException(ErrorCode.INVALID_GOOGLE_TOKEN));

        assertThatThrownBy(() -> authService.login(new LoginRequest(idToken)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_GOOGLE_TOKEN.getMessage());

        verify(googleOAuthClient).getUserInfo(idToken);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("GoogleOAuthClient 호출 중 네트워크 오류가 발생하면 예외가 전파된다")
    void login_googleOAuthClientNetworkError_throwsException() {
        String idToken = "fake-id-token";

        when(googleOAuthClient.getUserInfo(idToken))
                .thenThrow(new RuntimeException("Google API network error"));

        assertThatThrownBy(() -> authService.login(new LoginRequest(idToken)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Google API network error");

        verify(googleOAuthClient).getUserInfo(idToken);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("신규 사용자 저장 중 DB 예외가 발생하면 예외가 전파된다")
    void login_newUserSaveFailed_throwsException() {
        String idToken = "fake-id-token";
        String googleId = "google-sub-123";
        String email = "user@korea.ac.kr";

        when(googleOAuthClient.getUserInfo(idToken))
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
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> authService.login(new LoginRequest(idToken)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("탈퇴한 사용자가 재로그인하면 계정이 복구된다")
    void login_deletedUser_restoresUser() throws Exception {
        String idToken = "fake-id-token";
        String googleId = "google-sub-123";
        String email = "user@korea.ac.kr";

        User deletedUser = User.createGoogleUser(email, googleId);
        setId(deletedUser, 1L);
        deletedUser.withdraw();

        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.getDeletedAt()).isNotNull();

        when(googleOAuthClient.getUserInfo(idToken))
                .thenReturn(new GoogleUserInfo(
                        googleId,
                        email,
                        true,
                        "홍길동",
                        null
                ));

        when(userRepository.findByGoogleId(googleId))
                .thenReturn(Optional.of(deletedUser));

        LoginResponse response = authService.login(new LoginRequest(idToken));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(deletedUser.getDeletedAt()).isNull();
        assertThat(deletedUser.isActive()).isTrue();

        verify(userRepository).findByGoogleId(googleId);
        verify(userRepository, never()).save(any(User.class));
    }

    private void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }
}