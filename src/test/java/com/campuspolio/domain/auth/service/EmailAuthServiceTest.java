package com.campuspolio.domain.auth.service;

import com.campuspolio.domain.auth.dto.EmailAuthResponse;
import com.campuspolio.domain.auth.dto.EmailSendRequest;
import com.campuspolio.domain.auth.dto.EmailVerifyRequest;
import com.campuspolio.domain.auth.entity.EmailVerification;
import com.campuspolio.domain.auth.repository.EmailVerificationRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailAuthServiceTest {

    private UserRepository userRepository;
    private EmailVerificationRepository emailVerificationRepository;
    private EmailAuthService emailAuthService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailVerificationRepository = mock(EmailVerificationRepository.class);
        emailAuthService = new EmailAuthService(userRepository, emailVerificationRepository);
    }

    @Test
    @DisplayName("대학 이메일이면 인증번호를 생성하고 저장한다")
    void sendCode_universityEmail_success() throws Exception {
        User user = User.createGoogleUser("user@korea.ac.kr", "google-123", true);
        setId(user, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        EmailAuthResponse response = emailAuthService.sendCode(
                1L,
                new EmailSendRequest("user@korea.ac.kr")
        );

        assertThat(response.message()).isEqualTo("인증번호가 발송되었습니다.");

        ArgumentCaptor<EmailVerification> captor =
                ArgumentCaptor.forClass(EmailVerification.class);

        verify(emailVerificationRepository).save(captor.capture());

        EmailVerification saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@korea.ac.kr");
        assertThat(saved.getCode()).hasSize(6);
        assertThat(saved.getCode()).matches("[0-9]{6}");
    }

    @Test
    @DisplayName("로그인한 사용자 이메일과 요청 이메일이 다르면 예외가 발생한다")
    void sendCode_emailNotMatched_throwsException() throws Exception {
        User user = User.createGoogleUser("user@korea.ac.kr", "google-123", true);
        setId(user, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailAuthService.sendCode(
                1L,
                new EmailSendRequest("other@korea.ac.kr")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_NOT_MATCHED.getMessage());
    }

    @Test
    @DisplayName("대학 이메일(.ac.kr)이 아니면 예외가 발생한다")
    void sendCode_notUniversityEmail_throwsException() throws Exception {
        User user = User.createGoogleUser("user@gmail.com", "google-123", false);
        setId(user, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailAuthService.sendCode(
                1L,
                new EmailSendRequest("user@gmail.com")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_NOT_UNIVERSITY_DOMAIN.getMessage());
    }

    @Test
    @DisplayName("인증번호가 일치하면 이메일 인증이 완료된다")
    void verifyCode_success() throws Exception {
        User user = User.createGoogleUser("user@korea.ac.kr", "google-123", true);
        setId(user, 1L);

        EmailVerification verification = EmailVerification.create(
                "user@korea.ac.kr",
                "123456",
                LocalDateTime.now().plusMinutes(5)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc("user@korea.ac.kr"))
                .thenReturn(Optional.of(verification));

        EmailAuthResponse response = emailAuthService.verifyCode(
                1L,
                new EmailVerifyRequest("user@korea.ac.kr", "123456")
        );

        assertThat(response.message()).isEqualTo("이메일 인증이 완료되었습니다.");
        assertThat(user.isVerified()).isTrue();
        assertThat(verification.isVerified()).isTrue();
    }

    @Test
    @DisplayName("인증번호가 만료되면 예외가 발생한다")
    void verifyCode_expired_throwsException() throws Exception {
        User user = User.createGoogleUser("user@korea.ac.kr", "google-123", true);
        setId(user, 1L);

        EmailVerification verification = EmailVerification.create(
                "user@korea.ac.kr",
                "123456",
                LocalDateTime.now().minusMinutes(1)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc("user@korea.ac.kr"))
                .thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> emailAuthService.verifyCode(
                1L,
                new EmailVerifyRequest("user@korea.ac.kr", "123456")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_VERIFICATION_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 예외가 발생한다")
    void verifyCode_mismatch_throwsException() throws Exception {
        User user = User.createGoogleUser("user@korea.ac.kr", "google-123", true);
        setId(user, 1L);

        EmailVerification verification = EmailVerification.create(
                "user@korea.ac.kr",
                "123456",
                LocalDateTime.now().plusMinutes(5)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc("user@korea.ac.kr"))
                .thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> emailAuthService.verifyCode(
                1L,
                new EmailVerifyRequest("user@korea.ac.kr", "999999")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
    }

    private void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }
}