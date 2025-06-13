package moneybuddy.domain.consultation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.domain.consultation.entity.ConsultationMessage;
import moneybuddy.global.enums.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅방 메시지 조회 응답 DTO입니다.
 * 단일 메시지의 상세 정보를 포함하며, 메시지 ID, 보낸 사람 정보, 메시지 타입, 읽음 여부 등을 포함합니다.
 */
@Schema(description = "채팅 메시지 조회 응답 DTO")
public record ConsultationMessageResponseDto(

    @Schema(description = "메시지 ID", example = "12345")
    Long messageId,

    @Schema(description = "보낸 사람 ID", example = "7")
    Long senderId,

    @Schema(description = "보낸 사람 닉네임", example = "dohyunnn")
    String senderNickname,

    @Schema(description = "보낸 사람 프로필 이미지 URL", example = "https://...")
    String senderProfileImage,

    @Schema(description = "텍스트 메시지 내용", example = "안녕하세요")
    String message,

    @Schema(description = "이미지 메시지 URL", example = "https://...")
    String imageUrl,

    @Schema(description = "메시지 타입", example = "TEXT")
    MessageType type,

    @Schema(description = "메시지 전송 시간", example = "2025-04-25T15:30:00")
    LocalDateTime sentAt,

    @Schema(description = "읽음 여부", example = "true")
    boolean isRead
) {
    public static ConsultationMessageResponseDto from(ConsultationMessage entity) {
        return new ConsultationMessageResponseDto(
            entity.getId(),
            entity.getSender() != null ? entity.getSender().getId() : null,
            entity.getSender() != null ? entity.getSender().getNickname() : null,
            entity.getSender() != null ? entity.getSender().getProfileImage() : null,
            entity.getMessage(),
            entity.getImageUrl(),
            entity.getType(),
            entity.getSentAt(),
            entity.isRead()
        );
    }
}
