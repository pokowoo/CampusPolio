package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import com.campuspolio.global.security.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 핵심: Security 완전 비활성화
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("accessToken이 없으면 400 Bad Request를 반환한다")
    void login_withoutAccessToken_returnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "accessToken": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 시 userId를 세션에 저장하고 사용자 정보를 반환한다")
    void login_success_setsSessionAndReturnsUserInfo() throws Exception {

        LoginResponse response = new LoginResponse(
                1L,
                "user@syu.ac.kr",
                true,
                false
        );

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "accessToken": "fake-google-access-token"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@syu.ac.kr"))
                .andExpect(jsonPath("$.data.isDomainValid").value(true))
                .andExpect(jsonPath("$.data.isVerified").value(false))
                .andExpect(request().sessionAttribute(SessionConst.LOGIN_USER_ID, 1L));
    }
}