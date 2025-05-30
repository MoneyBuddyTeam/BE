package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공개 범위")
public enum PrivacyLevel {
    PUBLIC,
    PRIVATE
}
