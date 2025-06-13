package moneybuddy.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * Redis 채널 구독을 동적으로 관리하는 서비스
 * (채팅방이 생성될 때마다 해당 방의 토픽을 Redis 구독)
 */
@Service
@RequiredArgsConstructor
public class RedisSubscriberService {

    private final RedisMessageListenerContainer container;
    private final MessageListenerAdapter listenerAdapter;

    /**
     * 채팅방(roomId)별로 Redis 채널을 동적으로 구독
     *
     * @param consultationRoomId 채팅방 ID
     */
    public void subscribeChatRoom(Long consultationRoomId) {
        String topicName = "consultation-room:" + consultationRoomId;
        ChannelTopic topic = new ChannelTopic(topicName);

        // 이미 구독 중인지 확인하고 추가하는 로직을 넣을 수도 있지만,
        // 기본은 단순 등록 (필요 시 개선)
        container.addMessageListener(listenerAdapter, topic);
    }
}
