
package com.campuspolio.domain.profile.service;

import com.campuspolio.domain.profile.dto.ProfileCreateRequest;
import com.campuspolio.domain.profile.dto.ProfileUpdateRequest;
import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:profiletest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
@EntityScan(basePackages = "com.campuspolio.domain")
@Import(ProfileService.class)
class ProfileServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private EntityManager entityManager;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.saveAndFlush(
                User.createGoogleUser(
                        "integration@test.com",
                        "integration-google-id"
                )
        );

        userId = user.getId();

        profileService.createProfile(
                userId,
                new ProfileCreateRequest(
                        "기존닉네임",
                        "기존 자기소개"
                )
        );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow();

        profile.update(
                "홍길동",
                "기존닉네임",
                "기존 자기소개",
                "컴퓨터공학과",
                4,
                "https://example.com/profile.png"
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("실제 프로필 필드가 변경되면 DB에 저장되고 updatedAt이 갱신된다")
    void updateProfile_changedValues_updatesDatabase() {

        UserProfile before = findProfile();

        LocalDateTime oldUpdatedAt = before.getUpdatedAt();

        profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "김철수",
                        "새닉네임",
                        "새로운 자기소개",
                        "소프트웨어학과",
                        3,
                        "https://example.com/new-profile.png"
                )
        );

        entityManager.flush();
        entityManager.clear();

        UserProfile after = findProfile();

        assertThat(after.getName()).isEqualTo("김철수");
        assertThat(after.getNickname()).isEqualTo("새닉네임");
        assertThat(after.getBio()).isEqualTo("새로운 자기소개");
        assertThat(after.getMajor()).isEqualTo("소프트웨어학과");
        assertThat(after.getGrade()).isEqualTo(3);
        assertThat(after.getProfileImage())
                .isEqualTo("https://example.com/new-profile.png");

        assertThat(after.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    @DisplayName("동일한 값을 전달하면 DB의 updatedAt이 유지된다")
    void updateProfile_sameValues_preservesUpdatedAt() {

        UserProfile before = findProfile();

        LocalDateTime oldUpdatedAt = before.getUpdatedAt();

        profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        "홍길동",
                        "기존닉네임",
                        "기존 자기소개",
                        "컴퓨터공학과",
                        4,
                        "https://example.com/profile.png"
                )
        );

        entityManager.flush();
        entityManager.clear();

        UserProfile after = findProfile();

        assertThat(after.getName()).isEqualTo("홍길동");
        assertThat(after.getNickname()).isEqualTo("기존닉네임");

        assertThat(after.getUpdatedAt()).isEqualTo(oldUpdatedAt);
    }

    @Test
    @DisplayName("일부 필드만 수정하면 나머지 필드는 DB에서 유지된다")
    void  updateProfile_partialRequest_preservesOtherFields() {

        UserProfile before = findProfile();

        LocalDateTime oldUpdatedAt = before.getUpdatedAt();

        profileService.updateProfile(
                userId,
                new ProfileUpdateRequest(
                        null,
                        "부분수정닉네임",
                        null,
                        null,
                        null,
                        null
                )
        );

        entityManager.flush();
        entityManager.clear();

        UserProfile after = findProfile();

        assertThat(after.getName()).isEqualTo("홍길동");
        assertThat(after.getNickname()).isEqualTo("부분수정닉네임");
        assertThat(after.getBio()).isEqualTo("기존 자기소개");
        assertThat(after.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(after.getGrade()).isEqualTo(4);
        assertThat(after.getProfileImage())
                .isEqualTo("https://example.com/profile.png");

        assertThat(after.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    private UserProfile findProfile() {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow();
    }
}
