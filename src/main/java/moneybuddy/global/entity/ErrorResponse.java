package moneybuddy.global.entity;

import java.time.LocalDateTime;

import moneybuddy.global.enums.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "에러 응답")
@Builder
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus())
                .error(errorCode.getError())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }
}
