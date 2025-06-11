package moneybuddy.global.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 응답 객체. 성공 시 data를 포함하고,
 * 실패 시 error 정보를 포함한다.
 *
 * Swagger 문서화에는 직접 사용하지 않으며,
 * 성공 응답 DTO 또는 ApiErrorSchema를 사용하여 문서화한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private int status;
        private String error;
        private String message;
        private String path;
        private LocalDateTime timestamp;
    }

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> error(int status, String error, String message, String path) {
        return CommonResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .status(status)
                        .error(error)
                        .message(message)
                        .path(path)
                        .timestamp(LocalDateTime.now())
                        .build())
                .build();
    }
}
