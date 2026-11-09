
package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.AuthTokenResult;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.campuspolio.global.security.JwtProvider;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "jwt.access-token-expiration=900000",
                "jwt.refresh-token-expiration=1209600000"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("idToken이 없으면 400 Bad Request를 반환한다")
    void login_withoutIdToken_returnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 성공 시 사용자 정보와 JWT 쿠키를 반환한다")
    void login_success_returnsUserInfoAndJwtCookies() throws Exception {

        LoginResponse loginResponse = new LoginResponse(
                1L,
                "user@korea.ac.kr",
                false
        );

        AuthTokenResult tokenResult = new AuthTokenResult(
                loginResponse,
                "test-access-token",
                "test-refresh-token"
        );

        when(authService.login(any()))
                .thenReturn(tokenResult);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "fake-google-id-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@korea.ac.kr"))
                .andExpect(jsonPath("$.data.universityVerified").value(false))
                .andReturn();

        List<String> setCookies =
                result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookies)
                .anySatisfy(cookie ->
                        assertThat(cookie)
                                .contains("accessToken=test-access-token")
                                .contains("HttpOnly")
                );

        assertThat(setCookies)
                .anySatisfy(cookie ->
                        assertThat(cookie)
                                .contains("refreshToken=test-refresh-token")
                                .contains("HttpOnly")
                );

        verify(authService).login(any());
    }

    @Test
    @DisplayName("Refresh Token 쿠키가 있으면 로그아웃하고 JWT 쿠키를 삭제한다")
    void logout_withRefreshToken_successAndDeletesCookies() throws Exception {

        doNothing().when(authService).logout("test-refresh-token");

        MvcResult result = mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(
                                "refreshToken",
                                "test-refresh-token"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        verify(authService).logout("test-refresh-token");

        List<String> setCookies =
                result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookies)
                .anySatisfy(cookie ->
                        assertThat(cookie)
                                .contains("accessToken=")
                                .contains("Max-Age=0")
                );

        assertThat(setCookies)
                .anySatisfy(cookie ->
                        assertThat(cookie)
                                .contains("refreshToken=")
                                .contains("Max-Age=0")
                );
    }

    @Test
    @DisplayName("Refresh Token 쿠키가 없어도 로그아웃 요청은 처리된다")
    void logout_withoutRefreshToken_success() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout(null);
    }
}
