package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Schema(description = "에러코드 종류(상태번호, 에러코드 이름, 에러코드 메세지)")
@Getter
public enum ErrorCode {

    // 400 BAD REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 항목이 누락되었습니다."),
    INVALID_LOGIN(HttpStatus.BAD_REQUEST, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    USER_DELETED(HttpStatus.FORBIDDEN, "USER_DELETED", "탈퇴한 계정입니다. 복구가 가능합니다."),
    PHONE_DELETED(HttpStatus.FORBIDDEN, "PHONE_DELETED", "탈퇴한 계정의 전화번호입니다."),

    // 404 NOT FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "탈퇴한지 30일이 지나 복구할 수 없는 계정이거나, 존재하지 않는 계정입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "존재하지 않는 리뷰입니다."),
    USER_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_SETTINGS_NOT_FOUND", "사용자 설정 정보가 존재하지 않습니다."),

    // 409 CONFLICT
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "이미 가입된 이메일입니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "이미 사용 중인 전화번호입니다."),

    // 410 GONE
    USER_DELETION_EXPIRED(HttpStatus.GONE, "USER_DELETION_EXPIRED", "복구 가능한 기간(30일)이 지났습니다."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String error;
    private final String message;

    ErrorCode(HttpStatus status, String error, String message) {
        this.status = status.value();
        this.error = error;
        this.message = message;
    }
}
