
package com.campuspolio.global.security;

import com.campuspolio.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserArgumentResolverTest {

    private static final Long USER_ID = 1L;

    private AuthenticatedUserArgumentResolver resolver;
    private MethodParameter authenticatedUserParameter;
    private MethodParameter normalParameter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        resolver = new AuthenticatedUserArgumentResolver();

        Method authenticatedMethod =
                TestController.class.getDeclaredMethod("authenticated", Long.class);

        authenticatedUserParameter = new MethodParameter(authenticatedMethod, 0);

        Method normalMethod =
                TestController.class.getDeclaredMethod("normal", Long.class);

        normalParameter = new MethodParameter(normalMethod, 0);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("@AuthenticatedUser가 붙은 Long 파라미터를 지원한다")
    void supportsParameter_authenticatedUser_returnsTrue() {
        // when
        boolean result = resolver.supportsParameter(authenticatedUserParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("@AuthenticatedUser가 없는 파라미터는 지원하지 않는다")
    void supportsParameter_withoutAnnotation_returnsFalse() {
        // when
        boolean result = resolver.supportsParameter(normalParameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("인증된 사용자의 ID를 반환한다")
    void resolveArgument_authenticatedUser_returnsUserId() {
        // given
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        USER_ID,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // when
        Object result = resolver.resolveArgument(
                authenticatedUserParameter,
                null,
                null,
                null
        );

        // then
        assertThat(result).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외를 발생시킨다")
    void resolveArgument_withoutAuthentication_throwsException() {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(
                authenticatedUserParameter,
                null,
                null,
                null
        )).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("인증되지 않은 Authentication이면 예외를 발생시킨다")
    void resolveArgument_unauthenticated_throwsException() {
        // given
        Authentication authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        USER_ID,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(
                authenticatedUserParameter,
                null,
                null,
                null
        )).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("Principal이 Long 타입이 아니면 예외를 발생시킨다")
    void resolveArgument_invalidPrincipal_throwsException() {
        // given
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@example.com",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(
                authenticatedUserParameter,
                null,
                null,
                null
        )).isInstanceOf(CustomException.class);
    }

    private static class TestController {

        public void authenticated(@AuthenticatedUser Long userId) {
        }

        public void normal(Long userId) {
        }
    }
}
