package moneybuddy.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import moneybuddy.domain.auth.entity.RefreshToken;
import moneybuddy.domain.auth.repository.OauthTokenRepository;
import moneybuddy.domain.auth.repository.RefreshTokenRepository;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private OauthTokenRepository oauthTokenRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .password("encoded-password") // 인코딩된 비밀번호라고 가정
                .nickname("tester")
                .loginMethod(moneybuddy.global.enums.LoginMethod.EMAIL)
                .isDeleted(false)
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .token("valid-refresh-token")
                .userId(testUser.getId())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
    }

    @Nested
    @DisplayName("1. Refresh Token 재발급")
    class RefreshTokenTests {

        @Test
        @DisplayName("유효한 RefreshToken으로 AccessToken 재발급 성공")
        void refreshTokenSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new MockCookie("refresh_token", "valid-refresh-token")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Access Token 재발급 완료"));
        }

        @Test
        @DisplayName("RefreshToken 누락 시 401 반환")
        void refreshTokenMissing() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Refresh Token 누락됨"));
        }

        @Test
        @DisplayName("RefreshToken 만료 시 401 반환")
        void refreshTokenExpired() throws Exception {
            refreshTokenRepository.save(RefreshToken.builder()
                    .token("expired-token")
                    .userId(testUser.getId())
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build());

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new MockCookie("refresh_token", "expired-token")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Refresh Token 만료됨"));
        }
    }

    // Logout 및 Unlink 테스트는 이후 확장
}
