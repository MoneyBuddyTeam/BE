package moneybuddy.domain.consultation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.ConsultationImageUploadResponseDto;
import moneybuddy.domain.consultation.dto.ConsultationMessageDto;
import moneybuddy.domain.consultation.service.ConsultationMessageService;
import moneybuddy.domain.user.entity.User;
import moneybuddy.util.RedisPublisher;
import moneybuddy.util.S3Uploader;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 상담 채팅 메시지 및 이미지 업로드 컨트롤러
 * 실시간 WebSocket 채팅 및 이미지 업로드 API를 제공합니다.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/consultation")
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;
    private final S3Uploader s3Uploader;
    private final RedisPublisher redisPublisher;

    /**
     * WebSocket 메시지 수신 처리
     * 클라이언트가 /chat 주소로 보낸 메시지를 수신하고 Redis Pub/Sub으로 발행합니다.
     * 메시지 저장과 발행을 동시에 처리합니다.
     *
     * @param messageDto 클라이언트로부터 수신한 메시지 DTO
     * @param message WebSocket Message 객체 (세션 정보 포함)
     */
    @MessageMapping("/chat")
    public void publishMessage(ConsultationMessageDto messageDto, Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        User loginUser = (User) accessor.getSessionAttributes().get("user");

        if (loginUser == null) {
            throw new IllegalArgumentException("WebSocket 인증 실패: 로그인 사용자 없음");
        }

        log.info("[Message Received] content: {}", messageDto.message());

        // 로그인한 사용자 ID를 강제로 주입
        ConsultationMessageDto updatedMessage = new ConsultationMessageDto(
                messageDto.consultationRoomId(),
                loginUser.getId(),
                messageDto.senderNickname(),
                messageDto.message(),
                messageDto.type(),
                messageDto.imageUrl(),
                messageDto.sentAt()
        );

        // Redis 저장 및 발행
        consultationMessageService.saveMessage(updatedMessage);
        ChannelTopic topic = new ChannelTopic("consultationRoom:" + updatedMessage.consultationRoomId());
        redisPublisher.publish(topic, updatedMessage);

        log.info("🔹 Redis Publish to: consultationRoom:{} with message: {}",
                updatedMessage.consultationRoomId(), updatedMessage.message());
    }

    /**
     * 상담 이미지 업로드 API
     * 채팅 메시지에 첨부할 이미지를 Amazon S3에 업로드하고 해당 URL을 반환합니다.
     *
     * @param consultationRoomId 이미지 업로드 대상 상담방 ID
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 URL을 포함한 DTO (200 OK)
     */
    @Operation(summary = "상담 이미지 업로드", description = "상담방 내에서 사용할 이미지를 업로드하고 URL을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업로드 성공",
            content = @Content(schema = @Schema(implementation = ConsultationImageUploadResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/{consultationRoomId}/image")
    public ResponseEntity<ConsultationImageUploadResponseDto> uploadConsultationImage(
            @Parameter(description = "이미지를 업로드할 상담방 ID", example = "1")
            @PathVariable("consultationRoomId") Long consultationRoomId,

            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        String url = s3Uploader.uploadFile(file, "consultation-images");
        ConsultationImageUploadResponseDto responseDto = new ConsultationImageUploadResponseDto(url);
        return ResponseEntity.ok(responseDto);
    }
}
