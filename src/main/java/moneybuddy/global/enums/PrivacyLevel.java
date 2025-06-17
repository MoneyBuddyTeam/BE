package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공개 수준")
public enum PrivacyLevel {
    @Schema(description = "전체 공개")
    PUBLIC,
    @Schema(description = "비공개")
    PRIVATE
}
