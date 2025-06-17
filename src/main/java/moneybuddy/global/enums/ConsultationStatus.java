package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상담 상태")
public enum ConsultationStatus {
    @Schema(description = "상담 요청됨")
    RESERVED,
    @Schema(description = "일정 확정됨")
    SCHEDULED,
    @Schema(description = "완료됨")
    COMPLETED,
    @Schema(description = "취소됨")
    CANCELLED
}

