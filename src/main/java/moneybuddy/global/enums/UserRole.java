package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 역할")
public enum UserRole {
    @Schema(description = "인증 없이 접근 가능")
    PUBLIC,
    @Schema(description = "일반 사용자")
    USER,
    @Schema(description = "전문가")
    ADVISOR,
    @Schema(description = "관리자")
    ADMIN
}
