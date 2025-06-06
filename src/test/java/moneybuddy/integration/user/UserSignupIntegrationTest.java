package moneybuddy.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import moneybuddy.domain.user.dto.UserSignupRequestDto;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 회원가입_정상_요청_성공() throws Exception {
        UserSignupRequestDto request = UserSignupRequestDto.of(
                "newuser@example.com",
                "password123",
                "NewUser",
                "01012345678",
                "https://example.com/image.png"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.nickname").value("NewUser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.loginMethod").value("EMAIL"));
    }

    @Test
    void 중복된_이메일로_회원가입_시_예외반환() throws Exception {
        // 기존 사용자 저장
        User existing = User.builder()
                .email("duplicate@example.com")
                .password("hashedPassword")
                .nickname("Dup")
                .phone("01000000000")
                .loginMethod(LoginMethod.EMAIL)
                .role(UserRole.USER)
                .isDeleted(false)
                .build();
        userRepository.save(existing);

        // 요청
        UserSignupRequestDto request = UserSignupRequestDto.of(
                "duplicate@example.com",
                "password123",
                "DupAgain",
                "01012341234",
                "https://example.com/image2.png"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 또는 .isConflict() if you use 409
    }
}
