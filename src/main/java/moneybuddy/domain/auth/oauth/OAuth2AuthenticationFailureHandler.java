package moneybuddy.domain.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    // 실패 시 리다이렉트할 프론트엔드 페이지
    private static final String FAILURE_REDIRECT_URL = "http://localhost:8080/"; // 예시

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception
    ) throws IOException, ServletException {
        log.error("LOG: OAuth2 FAILED!: {}", exception.getMessage());

        // 에러 이유를 쿼리 파라미터로 전달 가능
        response.sendRedirect(FAILURE_REDIRECT_URL + "?error=" + exception.getMessage());
    }
}
