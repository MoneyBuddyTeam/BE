package moneybuddy.domain.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.util.JwtTokenProvider;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private static final String REDIRECT_URL = "http://localhost:8080/";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        String email = null;
        String nickname = null;
        String picture = null;

        try {
            switch (registrationId) {
                case "google" -> {
                    email = (String) oAuth2User.getAttributes().get("email");
                    nickname = (String) oAuth2User.getAttributes().get("name");
                    picture = (String) oAuth2User.getAttributes().get("picture");
                }
                case "kakao" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> account = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
                    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                    nickname = (String) profile.get("nickname");
                    picture = (String) profile.get("profile_image_url");
                }
                case "naver" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                    email = (String) responseMap.get("email");
                    nickname = (String) responseMap.get("name");
                    picture = (String) responseMap.get("profile_image");
                }
            }

            if (email == null) {
                log.info("이메일이 없어 회원가입 페이지로 리디렉션합니다.");

                request.getSession().setAttribute("oauth_signup", Map.of(
                        "nickname", nickname,
                        "picture", picture,
                        "provider", registrationId
                ));

                response.sendRedirect("http://localhost:3000/oauth/signup");
                return;
            }
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 추출 실패", e);
            response.sendRedirect("/"); // 또는 사용자 친화적인 오류 페이지, 예시: response.sendRedirect("/?error=oauth_failed");
            return;
        }


        final String finalEmail = email;

        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> new IllegalStateException("OAuth 로그인 유저 정보 없음: " + finalEmail));

        String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(60 * 60 * 24);
        response.addCookie(jwtCookie);

        log.info("JWT 토큰 발급 완료 - userId: {}, email: {}", user.getId(), user.getEmail());
        response.sendRedirect(REDIRECT_URL);
    }
}
