package com.campuspolio.domain.profile.service;

import com.campuspolio.domain.profile.dto.ProfileCreateRequest;
import com.campuspolio.domain.profile.dto.ProfileCreateResponse;
import com.campuspolio.domain.profile.dto.ProfileResponse;
import com.campuspolio.domain.profile.dto.ProfileUpdateRequest;
import com.campuspolio.domain.profile.dto.ProfileUpdateResponse;
import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        profileService = new ProfileService(userRepository, userProfileRepository);
    }

    @Test
    @DisplayName("내 프로필을 조회한다")
    void getMyProfile_success() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );
        user.verifyUniversity();

        setId(user, userId);

        UserProfile profile = UserProfile.create(
                user,
                "길동이",
                "백엔드 개발자"
        );

        setProfileId(profile, 10L);

        profile.update(
                "홍길동",
                "길동이",
                "백엔드 개발자",
                "컴퓨터공학과",
                4,
                "https://s3.amazonaws.com/profile.png"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        ProfileResponse response = profileService.getMyProfile(userId);

        assertThat(response.profileId()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.nickname()).isEqualTo("길동이");
        assertThat(response.bio()).isEqualTo("백엔드 개발자");
        assertThat(response.major()).isEqualTo("컴퓨터공학과");
        assertThat(response.grade()).isEqualTo(4);
        assertThat(response.profileImage())
                .isEqualTo("https://s3.amazonaws.com/profile.png");
    }

    @Test
    @DisplayName("프로필 조회 시 사용자가 없으면 예외가 발생한다")
    void getMyProfile_userNotFound_throwsException() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 조회 시 프로필이 없으면 예외가 발생한다")
    void getMyProfile_profileNotFound_throwsException() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PROFILE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필을 생성한다")
    void createProfile_success() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.existsByUserId(userId))
                .thenReturn(false);

        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> {
                    UserProfile profile = invocation.getArgument(0);
                    setProfileId(profile, 10L);
                    return profile;
                });

        ProfileCreateResponse response = profileService.createProfile(
                userId,
                new ProfileCreateRequest("길동이", "백엔드 개발자")
        );

        assertThat(response.profileId()).isEqualTo(10L);
        assertThat(response.message()).isEqualTo("프로필 생성 완료");

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("이미 프로필이 존재하면 생성할 수 없다")
    void createProfile_alreadyExists_throwsException() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.existsByUserId(userId))
                .thenReturn(true);

        assertThatThrownBy(() -> profileService.createProfile(
                userId,
                new ProfileCreateRequest("길동이", "백엔드 개발자")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PROFILE_ALREADY_EXISTS.getMessage());

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 프로필을 생성할 수 없다")
    void createProfile_userNotFound_throwsException() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.createProfile(
                userId,
                new ProfileCreateRequest("길동이", "백엔드 개발자")
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 생성 중 Repository 예외가 발생하면 예외가 전파된다")
    void createProfile_repositoryException_throwsException() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.existsByUserId(userId))
                .thenReturn(false);

        when(userProfileRepository.save(any(UserProfile.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> profileService.createProfile(
                userId,
                new ProfileCreateRequest("길동이", "백엔드 개발자")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    @DisplayName("프로필을 수정한다")
    void updateProfile_success() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        UserProfile profile = UserProfile.create(
                user,
                "길동이",
                "백엔드 개발자"
        );

        setProfileId(profile, 10L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        ProfileUpdateResponse response = profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "홍길동",
                        "길동이",
                        "백엔드 개발 및 AI 프로젝트를 진행합니다.",
                        "컴퓨터공학과",
                        4,
                        "https://s3.amazonaws.com/profile.png"
                )
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.message()).isEqualTo("프로필 수정 완료");

        assertThat(profile.getName()).isEqualTo("홍길동");
        assertThat(profile.getNickname()).isEqualTo("길동이");
        assertThat(profile.getBio())
                .isEqualTo("백엔드 개발 및 AI 프로젝트를 진행합니다.");
        assertThat(profile.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(profile.getGrade()).isEqualTo(4);
        assertThat(profile.getProfileImage())
                .isEqualTo("https://s3.amazonaws.com/profile.png");
    }

    @Test
    @DisplayName("프로필 수정 시 사용자가 없으면 예외가 발생한다")
    void updateProfile_userNotFound_throwsException() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "홍길동",
                        "길동이",
                        "백엔드 개발자",
                        "컴퓨터공학과",
                        4,
                        "https://s3.amazonaws.com/profile.png"
                )
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필이 없으면 수정할 수 없다")
    void updateProfile_notFound_throwsException() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "홍길동",
                        "길동이",
                        "백엔드 개발 및 AI 프로젝트를 진행합니다.",
                        "컴퓨터공학과",
                        4,
                        "https://s3.amazonaws.com/profile.png"
                )
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PROFILE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 수정 중 Repository 예외가 발생하면 예외가 전파된다")
    void updateProfile_repositoryException_throwsException() throws Exception {
        Long userId = 1L;

        User user = User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );

        setId(user, userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.findByUserId(userId))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "홍길동",
                        "길동이",
                        "백엔드 개발자",
                        "컴퓨터공학과",
                        4,
                        "https://s3.amazonaws.com/profile.png"
                )
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    private void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    private void setProfileId(UserProfile profile, Long id) throws Exception {
        Field idField = UserProfile.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(profile, id);
    }
}