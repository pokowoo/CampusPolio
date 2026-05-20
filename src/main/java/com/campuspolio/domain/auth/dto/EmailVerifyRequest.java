package com.campuspolio.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "학교 이메일 인증번호 검증 요청")
public record EmailVerifyRequest(

        @Schema(
                description = "인증번호를 받은 대학 이메일",
                example = "user@korea.ac.kr"
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "이메일로 발송된 6자리 인증번호",
                example = "123456"
        )
        @NotBlank(message = "인증번호는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code

) {
}