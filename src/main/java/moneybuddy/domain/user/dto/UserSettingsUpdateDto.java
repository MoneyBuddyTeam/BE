package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import moneybuddy.global.enums.PrivacyLevel;

@Getter
@Setter
@Schema(description = "사용자 설정 수정 요청 DTO")
public class UserSettingsUpdateDto {

    @Schema(description = "알림 수신 여부", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean notificationEnabled;

    @Schema(description = "개인정보 공개 수준", example = "PRIVATE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private PrivacyLevel privacyLevel;
}
