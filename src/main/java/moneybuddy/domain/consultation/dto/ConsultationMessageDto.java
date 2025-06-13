package moneybuddy.domain.consultation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.global.enums.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅 메시지를 송수신할 때 사용되는 DTO입니다.
 * WebSocket 통신 시 주로 사용되며, 텍스트/이미지/SYSTEM 메시지를 포함할 수 있습니다.
 */
@Schema(description = "채팅 메시지 송수신 DTO")
public record ConsultationMessageDto(

    @Schema(description = "채팅방 ID", example = "101", required = true)
    Long consultationRoomId,

    @Schema(description = "보낸 사람 ID", example = "5", required = true)
    Long senderId,

    @Schema(description = "보낸 사람 닉네임", example = "dohyunnn", required = true)
    String senderNickname,

    @Schema(description = "메시지 본문 내용", example = "안녕하세요! 거래 가능할까요?")
    String message,

    @Schema(
        description = "메시지 타입 (TEXT, IMAGE, SYSTEM)",
        example = "TEXT",
        required = true,
        implementation = MessageType.class
    )
    MessageType type,

    @Schema(description = "이미지 메시지일 경우 이미지 URL", example = "https://...jpg")
    String imageUrl,

    @Schema(description = "메시지 전송 시간", example = "2025-04-25T14:05:00")
    LocalDateTime sentAt
) {}

