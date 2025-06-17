package moneybuddy.domain.consultation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.*;
import moneybuddy.domain.consultation.service.ConsultationMessageService;
import moneybuddy.domain.consultation.service.ConsultationRoomService;
import moneybuddy.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상담 채팅방 관련 API 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consultation/rooms")
@RequiredArgsConstructor
public class ConsultationRoomController {

    private final ConsultationRoomService consultationRoomService;
    private final ConsultationMessageService consultationMessageService;

    /**
     * 상담 채팅방 생성
     * 내담자가 전문가 ID를 지정하여 상담 채팅방을 생성합니다.
     * 이미 동일한 사용자-전문가 조합의 방이 존재하면 기존 방 ID를 반환합니다.
     *
     * @param loginUser  로그인한 사용자 (내담자)
     * @param requestDto 전문가 ID를 포함한 요청 정보
     * @return 생성되거나 조회된 채팅방 ID (201 Created)
     */
    @Operation(summary = "상담 채팅방 생성", description = "내담자가 상담사를 지정하여 새로운 상담 채팅방을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공")
    })
    @PostMapping
    public ResponseEntity<Long> createConsultationRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser,
            @RequestBody ConsultationRoomCreateRequestDto requestDto
    ) {
        Long roomId = consultationRoomService.createOrGetRoom(loginUser.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomId);
    }

    /**
     * 상담 채팅방 목록 조회
     * 로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회합니다.
     * 상대방 정보, 마지막 메시지, 안 읽은 메시지 수 등을 포함할 수 있습니다.
     *
     * @param loginUser 로그인한 사용자
     * @return 채팅방 요약 정보 리스트 (200 OK)
     */
    @Operation(summary = "상담 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 상담 채팅방 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ConsultationRoomSummaryDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<ConsultationRoomSummaryDto>> getMyConsultationRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(consultationRoomService.getConsultationRoomForUser(loginUser));
    }

    /**
     * 상담 메시지 목록 조회
     * 채팅방 내 메시지를 페이지 단위로 조회합니다. 기본 정렬은 최신순이며, 무한 스크롤 등에 사용됩니다.
     *
     * @param roomId   조회할 상담 채팅방 ID
     * @param pageable 페이징 정보 (기본 size: 20, sort: sentAt DESC)
     * @return 메시지 페이지 객체 (200 OK)
     */
    @Operation(summary = "상담 메시지 목록 조회", description = "특정 상담방의 메시지를 페이지 단위로 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<ConsultationMessageDto>> getMessages(
            @Parameter(description = "상담 채팅방 ID", example = "1") @PathVariable Long roomId,
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ConsultationMessageDto> messages = consultationMessageService.getMessages(roomId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 상담 메시지 읽음 처리
     * 사용자가 채팅방에 입장했을 때 읽음 처리를 수행합니다.
     *
     * @param roomId    상담 채팅방 ID
     * @param loginUser 로그인한 사용자
     * @return 성공 시 204 No Content
     */
    @Operation(summary = "상담 채팅방 메시지 읽음 처리", description = "현재 로그인한 사용자가 해당 채팅방의 메시지를 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "읽음 처리 성공")
    })
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @Parameter(description = "상담 채팅방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        consultationRoomService.markMessagesAsRead(roomId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 상담 채팅방 상세 조회
     * 채팅방의 상대방 정보, 생성일, 메시지 수 등 상세 정보를 조회합니다.
     *
     * @param roomId    상담 채팅방 ID
     * @param loginUser 로그인한 사용자
     * @return 채팅방 상세 정보 DTO (200 OK)
     */
    @Operation(summary = "상담 채팅방 상세 조회", description = "특정 상담 채팅방의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ConsultationRoomDetailDto.class)))
    })
    @GetMapping("/{roomId}/detail")
    public ResponseEntity<ConsultationRoomDetailDto> getRoomDetail(
            @Parameter(description = "상담 채팅방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(consultationRoomService.getConsultationRoomDetail(roomId, loginUser));
    }

    /**
     * 상담 채팅방 나가기
     * 사용자가 해당 채팅방에서 나갑니다. 데이터는 유지되며 목록에서는 숨겨집니다 (soft delete).
     *
     * @param roomId    상담 채팅방 ID
     * @param loginUser 로그인한 사용자
     * @return 204 No Content
     */
    @Operation(summary = "상담 채팅방 나가기", description = "사용자가 해당 상담 채팅방에서 나갑니다. (Soft delete 방식)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "나가기 성공")
    })
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @Parameter(description = "상담 채팅방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        consultationMessageService.leaveConsultationRoom(roomId, loginUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 상태 변경", description = "내담자/상담사의 역할에 따라 상담을 확정/완료/취소할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "상담 또는 사용자 없음")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateConsultationStatus(
            @PathVariable Long id,
            @RequestBody ConsultationStatusUpdateRequest request
    ) {
        consultationRoomService.updateConsultationStatus(id, request.userId(), request.newStatus());
        return ResponseEntity.noContent().build();
    }
}

