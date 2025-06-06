package moneybuddy.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import moneybuddy.domain.user.dto.UserLoginRequestDto;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserLoginIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/api/v1/users/login";

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("LoginUser")
                .phone("01012345678")
                .profileImage(null)
                .role(UserRole.USER)
                .loginMethod(LoginMethod.EMAIL)
                .isDeleted(false)
                .build();
        userRepository.save(user);
    }

    @Test
    void 로그인_정상_요청_성공() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "login@example.com", "password123"
        );

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("."))); // JWT 구조 확인: header.payload.signature
    }

    @Test
    void 로그인_이메일_불일치_401_반환() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "notfound@example.com", "password123"
        );

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_비밀번호_불일치_401_반환() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "login@example.com", "wrongPassword"
        );

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_탈퇴_계정_401_반환() throws Exception {
        User deletedUser = User.builder()
                .email("withdrawn@example.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("WithdrawnUser")
                .role(UserRole.USER)
                .loginMethod(LoginMethod.EMAIL)
                .isDeleted(true)
                .build();
        userRepository.save(deletedUser);

        UserLoginRequestDto request = new UserLoginRequestDto(
                "withdrawn@example.com", "password123"
        );

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
