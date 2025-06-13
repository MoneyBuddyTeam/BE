package moneybuddy.unit.domain.config;

import jakarta.servlet.http.Cookie;
import moneybuddy.util.JwtTokenProvider;
import moneybuddy.domain.auth.oauth.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private moneybuddy.domain.user.repository.UserRepository userRepository;

    @Test
    void onAuthenticationSuccess_JWT쿠키설정_리디렉트_성공() throws Exception {
        // given
        String email = "test@example.com";

        // principal로 DefaultOAuth2User 생성
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", email),
                "email"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 리포지토리에서 유저 찾기 + 토큰 발급 mocking
        moneybuddy.domain.user.entity.User user = moneybuddy.domain.user.entity.User.builder()
                .id(1L)
                .email(email)
                .role(moneybuddy.global.enums.UserRole.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));
        when(jwtTokenProvider.createToken(eq(1L), eq("USER"))).thenReturn("mock.jwt.token");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        Cookie cookie = response.getCookie("token");
        assertNotNull(cookie);
        assertEquals("mock.jwt.token", cookie.getValue());
        assertEquals("/", cookie.getPath());

        assertEquals(302, response.getStatus());
        assertEquals("http://localhost:8080/", response.getRedirectedUrl());
    }
}
