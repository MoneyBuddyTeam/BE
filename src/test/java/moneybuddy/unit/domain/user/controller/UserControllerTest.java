package moneybuddy.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.service.UserService;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.PrivacyLevel;
import moneybuddy.global.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void login_정상_요청_성공() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("test@example.com", "password123");

        when(userService.login(requestDto)).thenReturn("mocked.jwt.token");

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked.jwt.token"));
    }

    @Test
    void login_비밀번호_불일치_예외() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("test@example.com", "wrongpassword");

        when(userService.login(requestDto))
                .thenThrow(new IllegalArgumentException("Invalid password"));

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid password")));
    }

    @Test
    void signup_정상_요청_성공() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        UserResponseDto responseDto = new UserResponseDto(
                1L,
                requestDto.email(),
                requestDto.phone(),
                requestDto.nickname(),
                requestDto.profileImage(),
                requestDto.role(),
                requestDto.loginMethod()
        );

        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void signup_이메일_중복_예외() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto(
                "duplicate@example.com",
                "password123",
                "01012345678",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenThrow(new IllegalStateException("Email is already registered"));

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already registered"));
    }

    @Test
    void signup_닉네임_중복_예외() throws Exception {
        UserSignupRequestDto requestDto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "duplicatedNick",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenThrow(new IllegalStateException("Nickname is already in use"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nickname is already in use"));
    }

    @Test
    void signup_전화번호_중복_예외() throws Exception {
        UserSignupRequestDto requestDto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01099998888",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenThrow(new IllegalStateException("Phone number is already in use"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number is already in use"));
    }

    @Test
    void signup_삭제된_유저_복구_기간_내_예외() throws Exception {
        UserSignupRequestDto requestDto = new UserSignupRequestDto(
                "deleted@example.com",
                "password123",
                "01012345678",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenThrow(new IllegalStateException("Deleted user cannot be reused yet"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deleted user cannot be reused yet"));
    }

    @Test
    void getUser_정상_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "test@example.com",
                "tester",
                "01012345678",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.getUserById(userId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void getUser_존재하지_않는_유저_예외() throws Exception {
        // given
        Long userId = 999L;

        when(userService.getUserById(userId))
                .thenThrow(new NoSuchElementException("User not found"));

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void updateUser_정상_수정_성공() throws Exception {
        // given
        Long userId = 1L;

        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "newNick", "01099998888", "newProfile.png"
        );

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "test@example.com",
                "newNick",
                "01099998888",
                "newProfile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.updateUser(eq(userId), any(UserUpdateRequestDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.nickname").value("newNick"))
                .andExpect(jsonPath("$.phone").value("01099998888"))
                .andExpect(jsonPath("$.profileImage").value("newProfile.png"));
    }

    @Test
    void updateUser_존재하지_않는_유저_예외() throws Exception {
        // given
        Long userId = 999L;
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "newNick", "01012341234", "new.png"
        );

        when(userService.updateUser(eq(userId), any(UserUpdateRequestDto.class)))
                .thenThrow(new NoSuchElementException("User not found"));

        // when & then
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void deleteUser_정상_삭제_성공() throws Exception {
        // given
        Long userId = 1L;

        // service는 void 메서드니까 특별히 설정할 필요 없음
        doNothing().when(userService).deleteUser(userId);

        // when & then
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_존재하지_않는_유저_예외() throws Exception {
        // given
        Long userId = 999L;

        doThrow(new NoSuchElementException("User not found"))
                .when(userService).deleteUser(userId);

        // when & then
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void getPublicProfile_정상_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        PublicProfileDto profileDto = new PublicProfileDto(
                userId,
                "tester",
                "profile.png"
        );

        when(userService.getPublicProfile(userId)).thenReturn(profileDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.profileImage").value("profile.png"));
    }

    @Test
    void getPublicProfile_존재하지_않는_유저_예외() throws Exception {
        // given
        Long userId = 999L;

        when(userService.getPublicProfile(userId))
                .thenThrow(new NoSuchElementException("User not found"));

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void getUserSettings_정상_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        UserSettingsDto settingsDto = new UserSettingsDto(true, PrivacyLevel.PRIVATE);

        when(userService.getSettings(userId)).thenReturn(settingsDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}/settings", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(true))
                .andExpect(jsonPath("$.privacyLevel").value("PRIVATE"));
    }

    @Test
    void getUserSettings_설정_없음_예외() throws Exception {
        Long userId = 999L;

        when(userService.getSettings(userId))
                .thenThrow(new NoSuchElementException("User settings not found"));

        mockMvc.perform(get("/api/v1/users/{id}/settings", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User settings not found"));
    }

    @Test
    void updateUserSettings_정상_수정_성공() throws Exception {
        // given
        Long userId = 1L;
        UserSettingsUpdateDto requestDto = new UserSettingsUpdateDto(false, PrivacyLevel.PUBLIC);
        UserSettingsDto responseDto = new UserSettingsDto(false, PrivacyLevel.PUBLIC);

        when(userService.updateSettings(eq(userId), any(UserSettingsUpdateDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/users/{id}/settings", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(false))
                .andExpect(jsonPath("$.privacyLevel").value("PUBLIC"));
    }

    @Test
    void updateUserSettings_설정_없음_예외() throws Exception {
        Long userId = 999L;
        UserSettingsUpdateDto requestDto = new UserSettingsUpdateDto(true, PrivacyLevel.PRIVATE);

        when(userService.updateSettings(eq(userId), any(UserSettingsUpdateDto.class)))
                .thenThrow(new NoSuchElementException("User settings not found"));

        mockMvc.perform(put("/api/v1/users/{id}/settings", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User settings not found"));
    }

    @Test
    void recoverUser_정상_복구_성공() throws Exception {
        // given
        UserRecoverRequestDto requestDto = new UserRecoverRequestDto("recover@example.com");

        UserResponseDto responseDto = new UserResponseDto(
                1L,
                "recover@example.com",
                "revivedNick",
                "01012345678",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userService.recover(requestDto.email())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("recover@example.com"))
                .andExpect(jsonPath("$.nickname").value("revivedNick"));
    }

    @Test
    void recoverUser_존재하지_않는_유저_예외() throws Exception {
        // given
        UserRecoverRequestDto requestDto = new UserRecoverRequestDto("notfound@example.com");

        when(userService.recover(requestDto.email()))
                .thenThrow(new NoSuchElementException("Deleted user not found"));

        // when & then
        mockMvc.perform(post("/api/v1/users/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Deleted user not found"));
    }

    @Test
    void recoverUser_복구기간_초과_예외() throws Exception {
        // given
        UserRecoverRequestDto requestDto = new UserRecoverRequestDto("expired@example.com");

        when(userService.recover(requestDto.email()))
                .thenThrow(new IllegalStateException("Deletion recovery period expired"));

        // when & then
        mockMvc.perform(post("/api/v1/users/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deletion recovery period expired"));
    }


}
