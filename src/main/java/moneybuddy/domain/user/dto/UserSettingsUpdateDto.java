package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.global.enums.PrivacyLevel;

@Schema(description = "사용자 설정 수정 요청 DTO")
public record UserSettingsUpdateDto(

    @Schema(description = "알림 수신 여부", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean notificationEnabled,

    @Schema(description = "개인정보 공개 수준", example = "PRIVATE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    PrivacyLevel privacyLevel

) {}
