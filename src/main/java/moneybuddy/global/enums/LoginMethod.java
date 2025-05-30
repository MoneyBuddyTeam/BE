package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 방식")
public enum LoginMethod {
    EMAIL,
    KAKAO,
    NAVER,
    GOOGLE
}
