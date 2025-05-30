package moneybuddy.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.global.entity.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 클래스입니다.
 * 모든 Controller에서 발생하는 예외를 하나의 클래스에서 일관되게 처리하며,
 * 응답 형식은 ApiResponse.error 구조로 반환됩니다.
 *
 * Swagger 문서화 시 이 클래스 자체는 노출되지 않지만,
 * 컨트롤러별 @ApiResponse에서 에러 응답 예시로 명시하는 기준이 됩니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 기반 DTO 필드 검증 실패 시 처리
     * - HTTP Status: 400 Bad Request
     * - 응답 형식: ApiResponse.error
     * - 에러 메시지: 각 필드별 메시지를 세미콜론으로 연결
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * form-data, query parameter 등의 @ModelAttribute 바인딩 실패 시 처리
     * - HTTP Status: 400 Bad Request
     * - 응답 형식: ApiResponse.error
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<CommonResponse<Void>> handleBindException(
            org.springframework.validation.BindException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * 잘못된 인자 전달 시 발생하는 예외 처리
     * - 예: service 내부에서 throw new IllegalArgumentException("메시지");
     * - HTTP Status: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 인증은 되었지만 권한이 없는 경우
     * - 예: Security에서 @PreAuthorize 또는 hasRole 등에 실패
     * - HTTP Status: 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", request.getRequestURI());
    }

    /**
     * 비즈니스 로직 전용 예외 처리
     * - 개발자가 정의한 GlobalException(ErrorCode 기반)을 처리
     * - ErrorCode에서 status, message, error 값을 지정
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<CommonResponse<Void>> handleGlobalException(
            GlobalException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.valueOf(ex.getErrorCode().getStatus()),
                ex.getErrorCode().getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * 처리되지 않은 모든 예외를 포괄 처리
     * - 서버 내부 오류
     * - HTTP Status: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        log.error("🔥 Unhandled Exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.", request.getRequestURI());
    }

    /**
     * 공통 에러 응답 빌더
     * - 상태 코드, 메시지, 요청 경로를 기반으로 ApiResponse.error 객체 생성
     */
    private ResponseEntity<CommonResponse<Void>> buildErrorResponse(HttpStatus status, String message, String path) {
        CommonResponse<Void> response = CommonResponse.error(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(response);
    }
}
