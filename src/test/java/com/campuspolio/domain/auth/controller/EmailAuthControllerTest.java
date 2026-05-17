package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.EmailAuthResponse;
import com.campuspolio.domain.auth.dto.EmailSendRequest;
import com.campuspolio.domain.auth.dto.EmailVerifyRequest;
import com.campuspolio.domain.auth.service.EmailAuthService;
import com.campuspolio.global.config.WebConfig;
import com.campuspolio.global.security.AuthenticatedUserArgumentResolver;
import com.campuspolio.global.security.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        AuthenticatedUserArgumentResolver.class
})
class EmailAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailAuthService emailAuthService;

    @Test
    @DisplayName("세션이 없으면 인증번호 발송 요청 시 401을 반환한다")
    void send_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@korea.ac.kr"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("세션이 있으면 인증번호 발송에 성공한다")
    void send_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(emailAuthService.sendCode(eq(1L), any(EmailSendRequest.class)))
                .thenReturn(new EmailAuthResponse("인증번호가 발송되었습니다."));

        mockMvc.perform(post("/api/auth/email/send")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@korea.ac.kr"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("인증번호가 발송되었습니다."));
    }

    @Test
    @DisplayName("세션이 있으면 이메일 인증에 성공한다")
    void verify_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(emailAuthService.verifyCode(eq(1L), any(EmailVerifyRequest.class)))
                .thenReturn(new EmailAuthResponse("이메일 인증이 완료되었습니다."));

        mockMvc.perform(post("/api/auth/email/verify")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@korea.ac.kr",
                          "code": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    @DisplayName("인증번호가 6자리 숫자가 아니면 400을 반환한다")
    void verify_invalidCode_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(post("/api/auth/email/verify")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@korea.ac.kr",
                          "code": "123"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}