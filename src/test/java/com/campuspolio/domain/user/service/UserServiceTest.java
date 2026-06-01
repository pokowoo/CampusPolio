package com.campuspolio.domain.user.service;

import com.campuspolio.domain.user.dto.UserMeResponse;
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
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("userId로 내 정보를 조회한다")
    void getMe_success() throws Exception {
        User user = User.createGoogleUser(
                "user@gmail.com",
                "google-sub-123"
        );
        setId(user, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserMeResponse response = userService.getMe(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@gmail.com");
        assertThat(response.universityVerified()).isFalse();

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 userId면 내 정보 조회 시 예외가 발생한다")
    void getMe_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 내 정보 조회 시 예외가 발생한다")
    void getMe_deletedUser_throwsException() throws Exception {
        User user = User.createGoogleUser(
                "user@gmail.com",
                "google-sub-123"
        );
        setId(user, 1L);
        user.withdraw();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getMe(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 시 deletedAt이 기록된다")
    void withdraw_success() throws Exception {
        User user = User.createGoogleUser(
                "user@gmail.com",
                "google-sub-123"
        );
        setId(user, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.isActive()).isFalse();

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 탈퇴 시 예외가 발생한다")
    void withdraw_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("이미 탈퇴한 사용자 탈퇴 시 예외가 발생한다")
    void withdraw_alreadyDeletedUser_throwsException() throws Exception {
        User user = User.createGoogleUser(
                "user@gmail.com",
                "google-sub-123"
        );
        setId(user, 1L);
        user.withdraw();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 중 Repository 예외가 발생하면 예외가 전파된다")
    void withdraw_repositoryException_throwsException() {
        when(userRepository.findById(1L))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(userRepository).findById(1L);
    }

    private void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }
}