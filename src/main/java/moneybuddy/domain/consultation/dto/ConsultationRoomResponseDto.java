package moneybuddy.domain.consultation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.domain.consultation.entity.ConsultationRoom;

import java.time.LocalDateTime;

@Schema(description = "상담방 생성/조회 응답 DTO")
public record ConsultationRoomResponseDto(

    @Schema(description = "상담방 ID", example = "1001")
    Long consultationRoomId,

    @Schema(description = "전문가 닉네임", example = "advisor01")
    String expertNickname,

    @Schema(description = "내담자 닉네임", example = "client88")
    String clientNickname,

    @Schema(description = "마지막 메시지", example = "안녕하세요. 상담 신청드립니다.")
    String lastMessage,

    @Schema(description = "마지막 메시지 전송 시각", example = "2025-04-25T15:45:00")
    LocalDateTime lastMessageAt

) {
    public static ConsultationRoomResponseDto from(ConsultationRoom room) {
        return new ConsultationRoomResponseDto(
            room.getId(),
            room.getConsultant().getNickname(), // 전문가
            room.getClient().getNickname(),  // 내담자
            room.getLastMessage(),
            room.getLastMessageAt()
        );
    }
}
