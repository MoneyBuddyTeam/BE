package moneybuddy.domain.consultation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상담방 상세 정보 응답 DTO")
public record ConsultationRoomDetailDto(

        @Schema(description = "상담방 ID", example = "1001")
        Long consultationRoomId,

        @Schema(description = "상담 주제", example = "불안장애")
        String topic,

        @Schema(description = "상대방 닉네임", example = "johnny92")
        String opponentNickname,

        @Schema(description = "상대방 프로필 이미지 URL", example = "https://cdn.moneybuddy.com/profiles/johnny92.jpg")
        String opponentProfileImage

) {
}
