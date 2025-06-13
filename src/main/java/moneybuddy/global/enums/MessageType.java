package moneybuddy.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채팅 메시지의 타입을 정의하는 열거형(Enum)입니다.
 * TEXT: 일반 텍스트 메시지
 * IMAGE: 이미지 메시지
 * SYSTEM: 시스템 알림 메시지 (예: 입장/퇴장 알림 등)
 */
@Schema(description = "채팅 메시지 타입")
public enum MessageType {

    @Schema(description = "일반 텍스트 메시지")
    TEXT,

    @Schema(description = "이미지 메시지")
    IMAGE,

    @Schema(description = "시스템 알림 메시지")
    SYSTEM
}
