package com.campuspolio.domain.profile.controller;

import com.campuspolio.domain.profile.dto.ProfileCreateRequest;
import com.campuspolio.domain.profile.dto.ProfileCreateResponse;
import com.campuspolio.domain.profile.dto.ProfileResponse;
import com.campuspolio.domain.profile.dto.ProfileUpdateRequest;
import com.campuspolio.domain.profile.dto.ProfileUpdateResponse;
import com.campuspolio.domain.profile.service.ProfileService;
import com.campuspolio.global.config.WebConfig;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.exception.GlobalExceptionHandler;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        AuthenticatedUserArgumentResolver.class,
        GlobalExceptionHandler.class
})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    @DisplayName("세션이 있으면 내 프로필을 조회한다")
    void getMyProfile_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(profileService.getMyProfile(1L))
                .thenReturn(new ProfileResponse(
                        10L,
                        1L,
                        "홍길동",
                        "길동이",
                        "백엔드 개발자",
                        "컴퓨터공학과",
                        4,
                        "https://s3.amazonaws.com/profile.png"
                ));

        mockMvc.perform(get("/api/profile")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileId").value(10))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.bio").value("백엔드 개발자"))
                .andExpect(jsonPath("$.data.major").value("컴퓨터공학과"))
                .andExpect(jsonPath("$.data.grade").value(4))
                .andExpect(jsonPath("$.data.profileImage").value("https://s3.amazonaws.com/profile.png"));
    }

    @Test
    @DisplayName("세션이 없으면 프로필 조회 시 401을 반환한다")
    void getMyProfile_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("프로필 조회 시 사용자가 없으면 404를 반환한다")
    void getMyProfile_userNotFound_returnsNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 999L);

        when(profileService.getMyProfile(999L))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/profile")
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("프로필 조회 시 프로필이 없으면 404를 반환한다")
    void getMyProfile_profileNotFound_returnsNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(profileService.getMyProfile(1L))
                .thenThrow(new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        mockMvc.perform(get("/api/profile")
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("세션이 없으면 프로필 생성 시 401을 반환한다")
    void createProfile_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "길동이",
                          "bio": "백엔드 개발자"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("세션이 있으면 프로필을 생성한다")
    void createProfile_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(profileService.createProfile(eq(1L), any(ProfileCreateRequest.class)))
                .thenReturn(new ProfileCreateResponse(
                        10L,
                        "프로필 생성 완료"
                ));

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "길동이",
                          "bio": "백엔드 개발자"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileId").value(10))
                .andExpect(jsonPath("$.data.message").value("프로필 생성 완료"));
    }

    @Test
    @DisplayName("nickname이 누락되면 프로필 생성 시 400을 반환한다")
    void createProfile_missingNickname_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "bio": "백엔드 개발자"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("닉네임이 비어 있으면 프로필 생성 시 400을 반환한다")
    void createProfile_blankNickname_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "",
                          "bio": "백엔드 개발자"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("nickname이 최대 길이를 초과하면 프로필 생성 시 400을 반환한다")
    void createProfile_tooLongNickname_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        String longNickname = "가".repeat(31);

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "%s",
                          "bio": "백엔드 개발자"
                        }
                        """.formatted(longNickname)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("bio가 최대 길이를 초과하면 프로필 생성 시 400을 반환한다")
    void createProfile_tooLongBio_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        String longBio = "a".repeat(501);

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "길동이",
                          "bio": "%s"
                        }
                        """.formatted(longBio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이미 프로필이 있으면 프로필 생성 시 409를 반환한다")
    void createProfile_alreadyExists_returnsConflict() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(profileService.createProfile(eq(1L), any(ProfileCreateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.PROFILE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "nickname": "길동이",
                          "bio": "백엔드 개발자"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("세션이 있으면 프로필을 수정한다")
    void updateProfile_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 5, 14, 0);

        when(profileService.updateProfile(eq(1L), any(ProfileUpdateRequest.class)))
                .thenReturn(new ProfileUpdateResponse(
                        1L,
                        updatedAt,
                        "프로필 수정 완료"
                ));

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "백엔드 개발 및 AI 프로젝트를 진행합니다.",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.message").value("프로필 수정 완료"));
    }

    @Test
    @DisplayName("세션이 없으면 프로필 수정 시 401을 반환한다")
    void updateProfile_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "백엔드 개발자",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("grade가 음수이면 프로필 수정 시 400을 반환한다")
    void updateProfile_negativeGrade_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "백엔드 개발자",
                          "major": "컴퓨터공학과",
                          "grade": -1,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("nickname이 최대 길이를 초과하면 프로필 수정 시 400을 반환한다")
    void updateProfile_tooLongNickname_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        String longNickname = "가".repeat(31);

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "%s",
                          "bio": "백엔드 개발자",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """.formatted(longNickname)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("bio가 최대 길이를 초과하면 프로필 수정 시 400을 반환한다")
    void updateProfile_tooLongBio_returnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        String longBio = "a".repeat(501);

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "%s",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """.formatted(longBio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("프로필 수정 시 사용자가 없으면 404를 반환한다")
    void updateProfile_userNotFound_returnsNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 999L);

        when(profileService.updateProfile(eq(999L), any(ProfileUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "백엔드 개발자",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("프로필 수정 시 프로필이 없으면 404를 반환한다")
    void updateProfile_profileNotFound_returnsNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);

        when(profileService.updateProfile(eq(1L), any(ProfileUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        mockMvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "홍길동",
                          "nickname": "길동이",
                          "bio": "백엔드 개발자",
                          "major": "컴퓨터공학과",
                          "grade": 4,
                          "profileImage": "https://s3.amazonaws.com/profile.png"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_NOT_FOUND.getMessage()));
    }
}