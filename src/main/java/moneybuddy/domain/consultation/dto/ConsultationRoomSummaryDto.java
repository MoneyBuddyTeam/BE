package moneybuddy.domain.consultation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "상담방 목록 요약 응답 DTO")
public record ConsultationRoomSummaryDto(

    @Schema(description = "상담방 ID", example = "1001")
    Long consultationRoomId,

    @Schema(description = "상담 주제", example = "우울증과 스트레스 관리")
    String topic,

    @Schema(description = "상대방 사용자 ID", example = "15")
    Long opponentUserId,

    @Schema(description = "상대방 닉네임", example = "jenny88")
    String opponentNickname,

    @Schema(description = "상대방 프로필 이미지 URL", example = "https://cdn.moneybuddy.com/profiles/jenny88.jpg")
    String opponentProfileImage,

    @Schema(description = "마지막 메시지 내용", example = "조금 늦을 수도 있어요")
    String lastMessage,

    @Schema(description = "마지막 메시지 전송 시간", example = "2025-04-25T16:30:00")
    LocalDateTime lastMessageAt,

    @Schema(description = "상담 종료 여부", example = "false")
    boolean isClosed,

    @Schema(description = "안 읽은 메시지 수", example = "2")
    Integer unreadCount

) {}
