package moneybuddy.domain.user.repository;

import moneybuddy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmailAndIsDeletedTrue(String email);

    Optional<User> findByPhoneAndIsDeletedTrue(String phone);

    List<User> findAllByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);
}