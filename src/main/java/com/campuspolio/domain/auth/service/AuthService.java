
package com.campuspolio.domain.auth.service;

import com.campuspolio.domain.auth.client.GoogleOAuthClient;
import com.campuspolio.domain.auth.dto.AuthTokenResult;
import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.entity.RefreshToken;
import com.campuspolio.domain.auth.repository.RefreshTokenRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthTokenResult login(LoginRequest request) {

        // 1. Google 사용자 인증
        var googleUserInfo =
                googleOAuthClient.getUserInfo(request.idToken());

        String googleId = googleUserInfo.sub();
        String email = googleUserInfo.email();

        validateGoogleInfo(googleId, email);

        // 2. 사용자 조회 또는 생성
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createUser(email, googleId));

        handleDeletedUser(user);

        // 3. JWT 생성
        String accessToken =
                jwtProvider.generateAccessToken(user.getId());

        String refreshToken =
                jwtProvider.generateRefreshToken(user.getId());

        // 4. Refresh Token 해시 생성
        String refreshTokenHash =
                jwtProvider.hashToken(refreshToken);

        // 5. Refresh Token 만료시간 계산
        LocalDateTime expiresAt =
                LocalDateTime.now().plusNanos(
                        refreshTokenExpiration * 1_000_000
                );

        // 6. Refresh Token DB 저장
        RefreshToken savedRefreshToken = RefreshToken.create(
                user,
                refreshTokenHash,
                expiresAt
        );

        refreshTokenRepository.save(savedRefreshToken);

        // 7. 로그인 응답 생성
        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.isUniversityVerified()
        );

        return new AuthTokenResult(
                loginResponse,
                accessToken,
                refreshToken
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
        return userRepository.save(
                User.createGoogleUser(email, googleId)
        );
    }

    private void handleDeletedUser(User user) {
        if (user.isDeleted()) {
            user.restore();
        }
    }
    @Transactional
    public void logout(String refreshToken) {

        // Refresh Token이 없으면 종료
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        // JWT 유효성 검사
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            return;
        }

        // Refresh Token 해시 생성
        String tokenHash = jwtProvider.hashToken(refreshToken);

        // DB에서 Refresh Token 조회
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(savedToken -> {
                    // 이미 폐기되었거나 만료된 토큰은 처리하지 않음
                    if (savedToken.isRevoked()
                            || savedToken.isExpired(LocalDateTime.now())) {
                        return;
                    }

                    // 토큰 폐기
                    savedToken.revoke();
                });
    }
    @Transactional
    public AuthTokenResult refresh(String refreshToken) {

        // 1. Refresh Token 존재 여부 확인
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 2. JWT 서명, 만료시간, 토큰 종류 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Refresh Token 해시 생성
        String tokenHash = jwtProvider.hashToken(refreshToken);

        // 4. DB에서 기존 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.UNAUTHORIZED));

        // 5. 폐기 여부 및 만료시간 확인
        if (savedToken.isRevoked()
                || savedToken.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        User user = savedToken.getUser();

        // 6. JWT의 사용자 ID와 DB 사용자 ID 일치 여부 확인
        Long tokenUserId = jwtProvider.getUserId(refreshToken);

        if (!user.getId().equals(tokenUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 7. 기존 Refresh Token 폐기
        savedToken.revoke();

        // 8. 새로운 JWT 생성
        String newAccessToken =
                jwtProvider.generateAccessToken(user.getId());

        String newRefreshToken =
                jwtProvider.generateRefreshToken(user.getId());

        // 9. 새로운 Refresh Token 해시 생성
        String newTokenHash =
                jwtProvider.hashToken(newRefreshToken);

        // 10. 새로운 Refresh Token 만료시간 계산
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusNanos(refreshTokenExpiration * 1_000_000);

        // 11. 새로운 Refresh Token DB 저장
        RefreshToken newRefreshTokenEntity = RefreshToken.create(
                user,
                newTokenHash,
                expiresAt
        );

        refreshTokenRepository.save(newRefreshTokenEntity);

        // 12. 로그인 응답 및 새 토큰 반환
        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.isUniversityVerified()
        );

        return new AuthTokenResult(
                loginResponse,
                newAccessToken,
                newRefreshToken
        );
    }
}
