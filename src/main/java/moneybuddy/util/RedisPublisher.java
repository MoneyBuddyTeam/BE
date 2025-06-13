package moneybuddy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.ConsultationMessageDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * 채팅 메시지를 Redis Topic으로 발행
     */
    public void publish(ChannelTopic topic, ConsultationMessageDto consultationMessageDto) {
        try {
            String json = objectMapper.writeValueAsString(consultationMessageDto);
            redisTemplate.convertAndSend(topic.getTopic(), json);
            log.info("RedisPublisher - Published message to topic {}: {}", topic.getTopic(), json);
        } catch (Exception e) {
            log.error("RedisPublisher - Error publishing message: {}", e.getMessage());
        }
    }
}
