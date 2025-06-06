package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.global.enums.PrivacyLevel;

@Schema(description = "사용자 설정 응답 DTO")
public record UserSettingsDto(

    @Schema(description = "알림 수신 여부", example = "true")
    Boolean notificationEnabled,

    @Schema(description = "개인정보 공개 수준", example = "PUBLIC")
    PrivacyLevel privacyLevel

) {}
