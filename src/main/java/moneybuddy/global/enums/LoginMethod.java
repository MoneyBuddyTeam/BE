package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

// 로그인 방식
@Schema(description = "로그인 방식")
public enum LoginMethod {
    @Schema(description = "카카오 로그인")
    KAKAO,
    @Schema(description = "네이버 로그인")
    NAVER,
    @Schema(description = "구글 로그인")
    GOOGLE,
    @Schema(description = "이메일 회원가입")
    EMAIL
}
