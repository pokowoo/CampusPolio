package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import com.campuspolio.global.security.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

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
    @DisplayName("로그인 성공 시 userId를 세션에 저장하고 사용자 정보를 반환한다")
    void login_success_setsSessionAndReturnsUserInfo() throws Exception {
        LoginResponse response = new LoginResponse(
                1L,
                "user@korea.ac.kr",
                false
        );

        when(authService.login(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
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
                .andExpect(request().sessionAttribute(
                        SessionConst.LOGIN_USER_ID,
                        1L
                ));
    }

    @Test
    @DisplayName("세션이 있으면 로그아웃에 성공하고 세션을 무효화한다")
    void logout_withSession_successAndInvalidatesSession() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("세션이 없어도 로그아웃 요청은 오류 없이 처리된다")
    void logout_withoutSession_success() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("세션이 있으면 로그아웃에 성공하고 세션을 무효화한다")
    void logout_withSession_successAndInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("세션이 없어도 로그아웃 요청은 오류 없이 처리된다")
    void logout_withoutSession_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}