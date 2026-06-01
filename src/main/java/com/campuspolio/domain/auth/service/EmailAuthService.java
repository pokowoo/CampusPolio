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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private static final String UNIVERSITY_EMAIL_DOMAIN_SUFFIX = ".ac.kr";
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public EmailAuthResponse sendCode(Long userId, EmailSendRequest request) {
        User user = getUser(userId);
        String email = request.email();

        validateUniversityEmail(email);

        if (user.isUniversityVerified()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        EmailVerification emailVerification = EmailVerification.create(email, code, expiresAt);
        emailVerificationRepository.save(emailVerification);

        log.info("[이메일 인증번호] email={}, code={}, expiresAt={}", email, code, expiresAt);

        return new EmailAuthResponse("인증번호가 발송되었습니다.");
    }

    @Transactional
    public EmailAuthResponse verifyCode(Long userId, EmailVerifyRequest request) {
        User user = getUser(userId);
        String email = request.email();

        validateUniversityEmail(email);

        if (user.isUniversityVerified()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerification emailVerification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        if (emailVerification.isExpired(now)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!emailVerification.isCodeMatched(request.code())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        emailVerification.verify();
        user.verifyUniversity();

        return new EmailAuthResponse("대학 인증이 완료되었습니다.");
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUniversityEmail(String email) {
        if (!isUniversityEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_UNIVERSITY_DOMAIN);
        }
    }

    private boolean isUniversityEmail(String email) {
        int atIndex = email.indexOf("@");

        if (atIndex == -1 || atIndex == email.length() - 1) {
            return false;
        }

        String domain = email.substring(atIndex + 1).toLowerCase();

        return domain.endsWith(UNIVERSITY_EMAIL_DOMAIN_SUFFIX);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }
}