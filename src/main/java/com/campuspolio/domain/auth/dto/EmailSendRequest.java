package com.campuspolio.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "학교 이메일 인증번호 발송 요청")
public record EmailSendRequest(

        @Schema(
                description = "인증번호를 발송할 대학 이메일",
                example = "user@korea.ac.kr"
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email

) {
}