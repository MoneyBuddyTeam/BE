package moneybuddy.config;

import lombok.RequiredArgsConstructor;
import moneybuddy.util.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	private final RedisSubscriber redisSubscriber;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Key를 문자열로 직렬화
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		return template;
	}

	@Bean
	public ChannelTopic channelTopic() {
		return new ChannelTopic("consultation-room"); // 기본 Topic 하나 등록
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter, ChannelTopic channelTopic) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		
		// 기본 Topic 하나 등록시: 임시
//        container.addMessageListener(listenerAdapter, channelTopic);
		
		// Topic 패턴 전체 등록
		container.addMessageListener(listenerAdapter, new PatternTopic("consultation-room:*"));
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter() {
		return new MessageListenerAdapter(redisSubscriber, "onMessage");
	}
}
