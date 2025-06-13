package moneybuddy.domain.consultation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.ConsultationMessageResponseDto;
import moneybuddy.domain.consultation.dto.ConsultationRoomDetailDto;
import moneybuddy.domain.consultation.dto.ConsultationRoomSummaryDto;
import moneybuddy.domain.consultation.service.ConsultationMessageService;
import moneybuddy.domain.consultation.service.ConsultationRoomService;
import moneybuddy.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상담 채팅방 관련 API 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consultation-rooms")
@RequiredArgsConstructor
public class ConsultationRoomController {

    private final ConsultationRoomService consultationRoomService;
    private final ConsultationMessageService consultationMessageService;

    @Operation(summary = "상담 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 상담 채팅방 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ConsultationRoomSummaryDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<ConsultationRoomSummaryDto>> getMyConsultationRooms(
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(consultationRoomService.getConsultationRoomForUser(loginUser));
    }

    @Operation(summary = "상담 채팅방 메시지 조회", description = "특정 상담 채팅방의 메시지를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ConsultationMessageResponseDto.class)))
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ConsultationMessageResponseDto>> getMessages(
        @Parameter(description = "상담 채팅방 ID", example = "1")
        @PathVariable Long roomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(consultationMessageService.getMessagesForConsultationRoom(roomId, loginUser));
    }

    @Operation(summary = "상담 채팅방 메시지 읽음 처리", description = "현재 로그인한 사용자가 해당 채팅방의 메시지를 읽음 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공")
    })
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
        @Parameter(description = "상담 채팅방 ID", example = "1")
        @PathVariable Long roomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        consultationRoomService.markMessagesAsRead(roomId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 채팅방 상세 조회", description = "특정 상담 채팅방의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ConsultationRoomDetailDto.class)))
    })
    @GetMapping("/{roomId}/detail")
    public ResponseEntity<ConsultationRoomDetailDto> getRoomDetail(
        @Parameter(description = "상담 채팅방 ID", example = "1")
        @PathVariable Long roomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(consultationRoomService.getConsultationRoomDetail(roomId, loginUser));
    }

    @Operation(summary = "상담 채팅방 나가기", description = "사용자가 해당 상담 채팅방에서 나갑니다. (Soft delete 방식)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "나가기 성공")
    })
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
        @Parameter(description = "상담 채팅방 ID", example = "1")
        @PathVariable Long roomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        consultationMessageService.leaveConsultationRoom(roomId, loginUser);
        return ResponseEntity.noContent().build();
    }
}
