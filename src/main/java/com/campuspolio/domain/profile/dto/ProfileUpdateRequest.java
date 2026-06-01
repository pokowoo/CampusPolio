package com.campuspolio.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(

        @Schema(description = "이름", example = "홍길동")
        @Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        @Size(max = 30, message = "닉네임은 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "자기소개", example = "백엔드 개발 및 AI 프로젝트를 진행합니다.")
        @Size(max = 500, message = "자기소개는 500자 이하로 입력해주세요.")
        String bio,

        @Schema(description = "전공", example = "컴퓨터공학과")
        @Size(max = 50, message = "전공은 50자 이하로 입력해주세요.")
        String major,

        @Schema(description = "학년", example = "4")
        @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
        @Max(value = 6, message = "학년은 6 이하이어야 합니다.")
        Integer grade,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://s3.amazonaws.com/profile.png"
        )
        String profileImage

) {
}