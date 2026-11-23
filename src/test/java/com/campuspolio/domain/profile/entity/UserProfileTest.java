
package com.campuspolio.domain.profile.entity;

import com.campuspolio.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    private User createUser() {
        return User.createGoogleUser(
                "user@korea.ac.kr",
                "google-123"
        );
    }

    private void setUpdatedAt(
            UserProfile profile,
            LocalDateTime updatedAt
    ) throws Exception {
        Field field = UserProfile.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(profile, updatedAt);
    }

    @Test
    @DisplayName("프로필 생성 시 초기값이 설정된다")
    void create_success() {
        User user = createUser();

        UserProfile profile = UserProfile.create(
                user,
                "길동이",
                "백엔드 개발자"
        );

        assertThat(profile.getUser()).isEqualTo(user);
        assertThat(profile.getNickname()).isEqualTo("길동이");
        assertThat(profile.getBio()).isEqualTo("백엔드 개발자");
        assertThat(profile.getName()).isNull();
        assertThat(profile.getMajor()).isNull();
        assertThat(profile.getGrade()).isNull();
        assertThat(profile.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("프로필의 모든 필드를 수정한다")
    void update_success() {
        UserProfile profile = UserProfile.create(
                createUser(),
                "기존닉네임",
                "기존 자기소개"
        );

        LocalDateTime oldUpdatedAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        try {
            setUpdatedAt(profile, oldUpdatedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        profile.update(
                "홍길동",
                "새닉네임",
                "새 자기소개",
                "컴퓨터공학과",
                4,
                "https://example.com/profile.png"
        );

        assertThat(profile.getName()).isEqualTo("홍길동");
        assertThat(profile.getNickname()).isEqualTo("새닉네임");
        assertThat(profile.getBio()).isEqualTo("새 자기소개");
        assertThat(profile.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(profile.getGrade()).isEqualTo(4);
        assertThat(profile.getProfileImage())
                .isEqualTo("https://example.com/profile.png");

        assertThat(profile.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    @DisplayName("일부 필드만 수정하면 나머지 필드와 기존 값이 유지된다")
    void update_partialRequest_preservesOtherFields() throws Exception {
        UserProfile profile = UserProfile.create(
                createUser(),
                "기존닉네임",
                "기존 자기소개"
        );

        profile.update(
                "홍길동",
                null,
                null,
                "컴퓨터공학과",
                4,
                null
        );

        assertThat(profile.getName()).isEqualTo("홍길동");
        assertThat(profile.getNickname()).isEqualTo("기존닉네임");
        assertThat(profile.getBio()).isEqualTo("기존 자기소개");
        assertThat(profile.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(profile.getGrade()).isEqualTo(4);
        assertThat(profile.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("모든 필드가 null이면 기존 데이터와 수정 시간이 유지된다")
    void update_allFieldsNull_preservesExistingValues() throws Exception {
        UserProfile profile = UserProfile.create(
                createUser(),
                "기존닉네임",
                "기존 자기소개"
        );

        profile.update(
                "홍길동",
                null,
                null,
                "컴퓨터공학과",
                4,
                "https://example.com/profile.png"
        );

        LocalDateTime oldUpdatedAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        setUpdatedAt(profile, oldUpdatedAt);

        profile.update(null, null, null, null, null, null);

        assertThat(profile.getName()).isEqualTo("홍길동");
        assertThat(profile.getNickname()).isEqualTo("기존닉네임");
        assertThat(profile.getBio()).isEqualTo("기존 자기소개");
        assertThat(profile.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(profile.getGrade()).isEqualTo(4);
        assertThat(profile.getProfileImage())
                .isEqualTo("https://example.com/profile.png");

        assertThat(profile.getUpdatedAt()).isEqualTo(oldUpdatedAt);
    }
    @Test
    @DisplayName("기존과 동일한 값을 전달하면 수정 시간은 유지된다")
    void update_sameValues_preservesUpdatedAt() throws Exception {
        UserProfile profile = UserProfile.create(
                createUser(),
                "기존닉네임",
                "기존 자기소개"
        );

        profile.update(
                "홍길동",
                "기존닉네임",
                "기존 자기소개",
                "컴퓨터공학과",
                4,
                "https://example.com/profile.png"
        );

        LocalDateTime oldUpdatedAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        setUpdatedAt(profile, oldUpdatedAt);

        profile.update(
                "홍길동",
                "기존닉네임",
                "기존 자기소개",
                "컴퓨터공학과",
                4,
                "https://example.com/profile.png"
        );

        assertThat(profile.getName()).isEqualTo("홍길동");
        assertThat(profile.getNickname()).isEqualTo("기존닉네임");
        assertThat(profile.getBio()).isEqualTo("기존 자기소개");
        assertThat(profile.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(profile.getGrade()).isEqualTo(4);
        assertThat(profile.getProfileImage())
                .isEqualTo("https://example.com/profile.png");

        assertThat(profile.getUpdatedAt()).isEqualTo(oldUpdatedAt);
    }
}
