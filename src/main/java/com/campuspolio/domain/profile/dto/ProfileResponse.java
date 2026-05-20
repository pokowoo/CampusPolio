package com.campuspolio.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 조회 응답")
public record ProfileResponse(

        @Schema(description = "프로필 ID", example = "1")
        Long profileId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        String nickname,

        @Schema(description = "자기소개", example = "백엔드 개발 및 AI 프로젝트를 진행합니다.")
        String bio,

        @Schema(description = "전공", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학년", example = "4")
        Integer grade,

        @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/profile.png")
        String profileImage

) {
}