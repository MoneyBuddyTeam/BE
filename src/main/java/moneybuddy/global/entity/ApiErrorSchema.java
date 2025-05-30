package moneybuddy.global.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "API 에러 응답 포맷")
public class ApiErrorSchema {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 코드", example = "INVALID_INPUT_VALUE")
    private String error;

    @Schema(description = "에러 메시지", example = "이메일은 필수입니다.")
    private String message;

    @Schema(description = "요청 경로", example = "/api/v1/users")
    private String path;

    @Schema(description = "에러 발생 시간", example = "2025-05-30T12:00:00")
    private LocalDateTime timestamp;
}
