package moneybuddy.domain.auth.service;// AuthService.java
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import moneybuddy.domain.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void logout(Long userId, HttpServletResponse response) {
        // Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // 쿠키 삭제 (token 이름으로)
        Cookie deleteToken = new Cookie("token", null);
        deleteToken.setPath("/");
        deleteToken.setHttpOnly(true);
        deleteToken.setMaxAge(0); // 즉시 삭제

        response.addCookie(deleteToken);
    }
}
