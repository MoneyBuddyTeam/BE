package moneybuddy.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisConnectionTest implements CommandLineRunner {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void run(String... args) throws Exception {
    try {
      redisTemplate.opsForValue().set("test:connection", "success");
      String result = (String) redisTemplate.opsForValue().get("test:connection");

      if ("success".equals(result)) {
        log.info("Redis 연결 성공.");
      } else {
        log.error("Redis 연결 실패: 값 불일치 (expected: success, actual: {})", result);
      }

      redisTemplate.delete("test:connection");
      log.debug("Redis 연결 테스트 키 정리 완료");
    } catch (Exception e) {
      log.error("Redis 연결 실패 - 에러 상세: {}", e.getMessage());
    }
  }
}
