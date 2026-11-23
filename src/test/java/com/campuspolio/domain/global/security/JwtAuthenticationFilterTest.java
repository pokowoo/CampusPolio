
package com.campuspolio.global.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final Long USER_ID = 1L;
    private static final String ACCESS_TOKEN = "valid-access-token";
    private static final String REFRESH_TOKEN = "valid-refresh-token";

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Access Token이면 SecurityContext에 사용자 ID를 저장한다")
    void doFilter_validAccessToken_authenticatesUser() throws Exception {
        // given
        MockHttpServletRequest request = createRequest(ACCESS_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateAccessToken(ACCESS_TOKEN))
                .thenReturn(true);
        when(jwtProvider.getUserId(ACCESS_TOKEN))
                .thenReturn(USER_ID);

        // when
        executeFilter(request, response);

        // then
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(USER_ID);
        assertThat(authentication.getAuthorities()).isEmpty();

        verify(jwtProvider).validateAccessToken(ACCESS_TOKEN);
        verify(jwtProvider).getUserId(ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Access Token 쿠키가 없으면 인증 정보를 설정하지 않는다")
    void doFilter_withoutToken_doesNotAuthenticate() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        executeFilter(request, response);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verifyNoInteractions(jwtProvider);
    }

    @Test
    @DisplayName("유효하지 않은 Access Token이면 인증하지 않는다")
    void doFilter_invalidAccessToken_doesNotAuthenticate() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateAccessToken("invalid-token"))
                .thenReturn(false);

        // when
        executeFilter(request, response);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtProvider).validateAccessToken("invalid-token");
        verify(jwtProvider, never()).getUserId(anyString());
    }

    @Test
    @DisplayName("만료된 Access Token이면 인증하지 않는다")
    void doFilter_expiredAccessToken_doesNotAuthenticate() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateAccessToken("expired-token"))
                .thenReturn(false);

        // when
        executeFilter(request, response);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtProvider).validateAccessToken("expired-token");
        verify(jwtProvider, never()).getUserId(anyString());
    }

    @Test
    @DisplayName("Refresh Token은 Access Token 인증에 사용할 수 없다")
    void doFilter_refreshToken_doesNotAuthenticate() throws Exception {
        // given
        MockHttpServletRequest request = createRequest(REFRESH_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateAccessToken(REFRESH_TOKEN))
                .thenReturn(false);

        // when
        executeFilter(request, response);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtProvider).validateAccessToken(REFRESH_TOKEN);
        verify(jwtProvider, never()).getUserId(anyString());
    }

    @Test
    @DisplayName("JWT 검증 중 예외가 발생하면 인증 정보를 제거한다")
    void doFilter_jwtException_clearsAuthentication() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("malformed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateAccessToken("malformed-token"))
                .thenThrow(new JwtException("Invalid JWT"));

        // when
        executeFilter(request, response);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtProvider).validateAccessToken("malformed-token");
        verify(jwtProvider, never()).getUserId(anyString());
    }

    @Test
    @DisplayName("토큰 검증 결과와 관계없이 다음 필터로 요청을 전달한다")
    void doFilter_invalidToken_continuesFilterChain() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainExecuted = new AtomicBoolean(false);

        when(jwtProvider.validateAccessToken("invalid-token"))
                .thenReturn(false);

        FilterChain filterChain = (req, res) ->
                chainExecuted.set(true);

        // when
        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        // then
        assertThat(chainExecuted).isTrue();
    }

    private MockHttpServletRequest createRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie(
                "accessToken",
                token
        ));
        return request;
    }

    private void executeFilter(
            MockHttpServletRequest request,
            MockHttpServletResponse response
    ) throws IOException, jakarta.servlet.ServletException {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                (req, res) -> {
                    // 다음 필터 실행
                }
        );
    }
}
