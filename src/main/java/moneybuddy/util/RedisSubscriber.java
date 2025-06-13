package moneybuddy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.ConsultationMessageDto;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            if (message.getBody() == null || message.getBody().length == 0) {
                log.warn("RedisSubscriber - 빈 메시지 수신");
                return;
            }

            String json = new String(message.getBody());
            ConsultationMessageDto consultationMessage = objectMapper.readValue(json, ConsultationMessageDto.class);

            // ✅ record 타입의 메서드 방식으로 수정
            messagingTemplate.convertAndSend("/sub/chat/room/" + consultationMessage.consultationRoomId(), consultationMessage);

            log.info("RedisSubscriber - roomId: {}, message: {}", consultationMessage.consultationRoomId(), consultationMessage.message());

        } catch (Exception e) {
            log.error("RedisSubscriber - 메시지 처리 중 에러", e);
        }
    }
}
