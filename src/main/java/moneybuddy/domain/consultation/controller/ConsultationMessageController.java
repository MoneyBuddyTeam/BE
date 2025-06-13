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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;
    private final S3Uploader s3Uploader;
    private final RedisPublisher redisPublisher;

    /**
     * 상담 메시지 수신 - Redis 발행
     * 프론트엔드에서 /chat/pub 경로로 전송된 메시지를 처리합니다.
     *
     * @param messageDto 클라이언트로부터 수신한 메시지 DTO
     * @param message WebSocket 세션 메시지
     */
    @MessageMapping("/chat/pub")
    public void publishMessage(ConsultationMessageDto messageDto, Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        User loginUser = (User) accessor.getSessionAttributes().get("user");

        if (loginUser == null) {
            throw new IllegalArgumentException("WebSocket 인증 실패: 로그인 사용자 없음");
        }

        log.info("[Message Received] content: {}", messageDto.message());

        // senderId 강제 주입하여 복사 생성
        ConsultationMessageDto updatedMessage = new ConsultationMessageDto(
                messageDto.consultationRoomId(),
                loginUser.getId(),
                messageDto.senderNickname(),
                messageDto.message(),
                messageDto.type(),
                messageDto.imageUrl(),
                messageDto.sentAt()
        );

        // 메시지 저장
        consultationMessageService.saveMessage(updatedMessage);

        // Redis 발행
        ChannelTopic topic = new ChannelTopic("consultationRoom:" + updatedMessage.consultationRoomId());
        redisPublisher.publish(topic, updatedMessage);
    }

    /**
     * 상담 이미지 업로드 API
     *
     * @param consultationRoomId 이미지 업로드 대상 상담방 ID
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 URL을 담은 DTO
     */
    @Operation(summary = "상담 이미지 업로드", description = "상담방 내에서 사용할 이미지를 업로드하고 URL을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공", content = @Content(schema = @Schema(implementation = ConsultationImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/consultation/{consultationRoomId}/image")
    public ResponseEntity<ConsultationImageUploadResponseDto> uploadConsultationImage(
            @Parameter(description = "이미지를 업로드할 상담방 ID", example = "1")
            @PathVariable("consultationRoomId") Long consultationRoomId,

            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file) {

        String url = s3Uploader.uploadFile(file, "consultation-images");
        ConsultationImageUploadResponseDto responseDto = new ConsultationImageUploadResponseDto(url);

        return ResponseEntity.ok(responseDto);
    }
}
