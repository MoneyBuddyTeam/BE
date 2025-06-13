package moneybuddy.domain.auth.repository;

import moneybuddy.domain.auth.entity.OAuthToken;
import moneybuddy.domain.user.entity.User;
import moneybuddy.global.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUserAndProvider(User user, OAuthProvider provider);
    void deleteByUserAndProvider(User user, OAuthProvider provider);
}
