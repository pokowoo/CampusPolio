
package com.campuspolio.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final Long USER_ID = 1L;

    private static final String RAW_SECRET =
            "0123456789abcdef0123456789abcdef";

    private static final String SECRET =
            Base64.getEncoder().encodeToString(
                    RAW_SECRET.getBytes(StandardCharsets.UTF_8)
            );

    private JwtProvider jwtProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                SECRET,
                60_000L,
                120_000L
        );

        secretKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(SECRET)
        );
    }

    @Test
    @DisplayName("정상적인 Access Token은 검증에 성공한다")
    void validateAccessToken_success() {
        // given
        String token = jwtProvider.generateAccessToken(USER_ID);

        // when
        boolean result = jwtProvider.validateAccessToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정상적인 Refresh Token은 검증에 성공한다")
    void validateRefreshToken_success() {
        // given
        String token = jwtProvider.generateRefreshToken(USER_ID);

        // when
        boolean result = jwtProvider.validateRefreshToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Refresh Token을 Access Token으로 검증하면 실패한다")
    void validateAccessToken_withRefreshToken_returnsFalse() {
        // given
        String token = jwtProvider.generateRefreshToken(USER_ID);

        // when
        boolean result = jwtProvider.validateAccessToken(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Access Token을 Refresh Token으로 검증하면 실패한다")
    void validateRefreshToken_withAccessToken_returnsFalse() {
        // given
        String token = jwtProvider.generateAccessToken(USER_ID);

        // when
        boolean result = jwtProvider.validateRefreshToken(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("만료된 Access Token은 검증에 실패한다")
    void validateAccessToken_expiredToken_returnsFalse() {
        // given
        String expiredToken = createExpiredAccessToken();

        // when
        boolean result = jwtProvider.validateAccessToken(expiredToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("서명이 위조된 토큰은 검증에 실패한다")
    void validateAccessToken_tamperedToken_returnsFalse() {
        // given
        String token = jwtProvider.generateAccessToken(USER_ID);

        char lastChar = token.charAt(token.length() - 1);
        char replacement = lastChar == 'a' ? 'b' : 'a';

        String tamperedToken =
                token.substring(0, token.length() - 1) + replacement;

        // when
        boolean result = jwtProvider.validateAccessToken(tamperedToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("정상적인 토큰에서 사용자 ID를 추출한다")
    void getUserId_success() {
        // given
        String token = jwtProvider.generateAccessToken(USER_ID);

        // when
        Long result = jwtProvider.getUserId(token);

        // then
        assertThat(result).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("잘못된 사용자 ID 형식의 토큰은 ID 추출에 실패한다")
    void getUserId_invalidSubject_throwsException() {
        // given
        String token = Jwts.builder()
                .subject("invalid-user-id")
                .claim("type", "access")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.getUserId(token))
                .isInstanceOf(NumberFormatException.class);
    }

    private String createExpiredAccessToken() {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(USER_ID.toString())
                .claim("type", "access")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
