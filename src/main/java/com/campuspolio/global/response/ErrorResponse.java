
package com.campuspolio.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답 형식")
public record ErrorResponse(

        @Schema(description = "요청 성공 여부", example = "false")
        boolean success,

        @Schema(description = "에러 코드", example = "UNAUTHORIZED")
        String code,

        @Schema(description = "에러 메시지", example = "로그인이 필요합니다.")
        String message

) {
}
