package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.user.dto.UserMeResponse;
import com.campuspolio.domain.user.service.UserService;
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
import com.campuspolio.global.config.WebConfig;
import com.campuspolio.global.security.AuthenticatedUserArgumentResolver;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        AuthenticatedUserArgumentResolver.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("세션이 없으면 내 정보 조회 시 401을 반환한다")
    void me_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("세션이 있으면 내 정보를 반환한다")
    void me_withSession_returnsUserInfo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        UserMeResponse response = new UserMeResponse(
                1L,
                "user@syu.ac.kr",
                true,
                false
        );

        when(userService.getMe(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@syu.ac.kr"))
                .andExpect(jsonPath("$.data.isDomainValid").value(true))
                .andExpect(jsonPath("$.data.isVerified").value(false));
    }
}