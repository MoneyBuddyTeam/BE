package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 역할(관리자, 전문가, 유저)")
public enum UserRole {
    ADMIN,
    ADVISOR,
    USER
}
