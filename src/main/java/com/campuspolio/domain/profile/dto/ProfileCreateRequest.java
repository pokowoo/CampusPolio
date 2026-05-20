package com.campuspolio.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 생성 요청")
public record ProfileCreateRequest(

        @Schema(description = "닉네임", example = "길동이")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "자기소개", example = "백엔드 개발자입니다.")
        @Size(max = 500, message = "자기소개는 500자 이하로 입력해주세요.")
        String bio

) {
}