package moneybuddy.domain.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.config.JwtTokenProvider;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 프론트엔드 주소: 로컬 or 운영 도메인
    private static final String REDIRECT_URL = "http://localhost:8080/"; // 예시

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("LOG: OAuth 로그인 유저 정보 없음: " + email));

        // JWT 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        // 방법 1: HTTP 응답 바디로 JSON 반환
        // response.setContentType("application/json");
        // response.getWriter().write("{\"token\": \"" + token + "\"}");

        // 방법 2: 쿠키에 저장
        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(60 * 60 * 24); // 1일
        response.addCookie(jwtCookie);

        System.out.println("JWT Token: " + token);

        response.sendRedirect(REDIRECT_URL);
    }
}
