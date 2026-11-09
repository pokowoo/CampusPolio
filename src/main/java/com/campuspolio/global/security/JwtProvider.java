
package com.campuspolio.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(Long userId) {
        return generateToken(userId, ACCESS, accessTokenExpiration);
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, REFRESH, refreshTokenExpiration);
    }

    // JWT 생성 공통 로직
    private String generateToken(
            Long userId,
            String type,
            long expiration
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .claim(TOKEN_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Access Token 검증
    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS);
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) {
        return validateToken(token, REFRESH);
    }

    // JWT 서명, 만료 시간, 토큰 종류 검증
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);

            return expectedType.equals(claims.get(TOKEN_TYPE, String.class))
                    && claims.getSubject() != null;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // JWT에서 사용자 ID 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);

        if (claims.getSubject() == null) {
            throw new IllegalArgumentException("JWT subject가 없습니다.");
        }

        return Long.parseLong(claims.getSubject());
    }

    // JWT Claim 파싱 및 서명 검증
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Refresh Token 원문 대신 SHA-256 해시 저장
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
