package moneybuddy.domain.user.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deleteExpiredUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> expiredUsers = userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(threshold);

        if (!expiredUsers.isEmpty()) {
            userRepository.deleteAll(expiredUsers);
            log.info("자동 삭제된 탈퇴 유저 수: {}", expiredUsers.size());
        } else {
            log.info("삭제 대상 탈퇴 유저 없음.");
        }
    }
}
