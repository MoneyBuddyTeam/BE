package moneybuddy.domain.user.service;

import moneybuddy.config.JwtTokenProvider;
import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.entity.UserSettings;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.domain.user.repository.UserSettingsRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.PrivacyLevel;
import moneybuddy.global.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void login_정상_요청_성공() {
        // given
        UserLoginRequestDto dto = new UserLoginRequestDto("test@example.com", "password123");
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .isDeleted(false)
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(dto.password(), mockUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(mockUser.getId(), mockUser.getRole().name())).thenReturn("mocked.jwt.token");

        // when
        String token = userService.login(dto);

        // then
        assertEquals("mocked.jwt.token", token);
    }

    @Test
    void login_존재하지_않는_이메일_예외() {
        // given
        UserLoginRequestDto dto = new UserLoginRequestDto("notfound@example.com", "password");
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> userService.login(dto));
    }

    @Test
    void login_탈퇴_회원_예외() {
        // given
        UserLoginRequestDto dto = new UserLoginRequestDto("deleted@example.com", "password");
        User deletedUser = User.builder()
                .email("deleted@example.com")
                .password("encodedPassword")
                .isDeleted(true)
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(deletedUser));

        // when & then
        assertThrows(IllegalStateException.class, () -> userService.login(dto));
    }

    @Test
    void login_비밀번호_불일치_예외() {
        // given
        UserLoginRequestDto dto = new UserLoginRequestDto("test@example.com", "wrongPassword");
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .isDeleted(false)
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.login(dto));
    }

    @ExtendWith(MockitoExtension.class)
    class UserServiceImplSignupTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserSettingsRepository userSettingsRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtTokenProvider jwtTokenProvider; // 사용 안 하지만 생성자에 필요

        @InjectMocks
        private UserServiceImpl userService;

        @Test
        void signup_정상_회원가입_성공() {
            // given
            UserSignupRequestDto dto = new UserSignupRequestDto(
                    "test@example.com",
                    "password123",
                    "01012345678",
                    "tester",
                    "profile.png",
                    UserRole.USER,
                    LoginMethod.EMAIL
            );

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(userRepository.findByPhone(dto.phone())).thenReturn(Optional.empty());
            when(userRepository.findByNickname(dto.nickname())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<UserSettings> settingsCaptor = ArgumentCaptor.forClass(UserSettings.class);

            // when
            UserResponseDto response = userService.signup(dto);

            // then
            verify(userRepository).save(userCaptor.capture());
            verify(userSettingsRepository).save(settingsCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(dto.email(), savedUser.getEmail());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertEquals(dto.nickname(), savedUser.getNickname());

            assertEquals(dto.email(), response.email());
            assertEquals(dto.nickname(), response.nickname());
        }

        @Test
        void signup_이메일_중복_삭제되지_않은_유저_존재_예외() {
            // given
            UserSignupRequestDto dto = new UserSignupRequestDto(
                    "test@example.com",
                    "password123",
                    "01012345678",
                    "tester",
                    "profile.png",
                    UserRole.USER,
                    LoginMethod.EMAIL
            );

            User existingUser = User.builder()
                    .email(dto.email())
                    .isDeleted(false)
                    .build();

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));

            // when & then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                userService.signup(dto);
            });

            assertEquals("Email is already registered", exception.getMessage());
        }

    }

    @Test
    void signup_삭제된_유저_존재_복구_기간_초과_정상_진행() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        User deletedUser = User.builder()
                .email(dto.email())
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(31)) // 복구 기한 초과
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(deletedUser));
        when(userRepository.findByPhone(dto.phone())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(dto.nickname())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");

        // when
        UserResponseDto response = userService.signup(dto);

        // then
        verify(userRepository).delete(deletedUser); // 삭제된 사용자 DB에서 제거
        verify(userRepository).save(any(User.class));
        verify(userSettingsRepository).save(any(UserSettings.class));
        assertEquals(dto.email(), response.email());
        assertEquals(dto.nickname(), response.nickname());
    }

    @Test
    void signup_삭제된_유저_존재_복구_기간_내_예외() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        User recentlyDeletedUser = User.builder()
                .email(dto.email())
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(10)) // 복구 가능 기간 내
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(recentlyDeletedUser));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.signup(dto);
        });

        assertEquals("Deleted user cannot be reused yet", exception.getMessage());
        verify(userRepository, never()).delete(any()); // 삭제 메서드가 호출되면 안 됨
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_전화번호_중복_예외() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678", // 중복 번호
                "tester",
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        User existingUser = User.builder()
                .phone(dto.phone())
                .isDeleted(false)
                .build();

        when(userRepository.findByPhone(dto.phone())).thenReturn(Optional.of(existingUser));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.signup(dto);
        });

        assertEquals("Phone number is already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_닉네임_중복_삭제되지_않은_유저_존재_예외() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "tester", // 중복 닉네임
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(dto.phone())).thenReturn(Optional.empty());

        User existingUser = User.builder()
                .nickname(dto.nickname())
                .isDeleted(false)
                .build();

        when(userRepository.findByNickname(dto.nickname())).thenReturn(Optional.of(existingUser));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.signup(dto);
        });

        assertEquals("Nickname is already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_닉네임_중복_삭제유저_복구기간_내_예외() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "password123",
                "01012345678",
                "tester", // 중복 닉네임
                "profile.png",
                UserRole.USER,
                LoginMethod.EMAIL
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(dto.phone())).thenReturn(Optional.empty());

        User deletedUserWithSameNickname = User.builder()
                .nickname(dto.nickname())
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(10)) // 복구 가능 기간 내
                .build();

        when(userRepository.findByNickname(dto.nickname())).thenReturn(Optional.of(deletedUserWithSameNickname));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.signup(dto);
        });

        assertEquals("Nickname is protected during recovery period", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void recover_삭제된_유저_복구_성공() {
        // given
        String email = "deleted@example.com";

        User deletedUser = User.builder()
                .email(email)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(10)) // 복구 가능
                .nickname("tester")
                .build();

        when(userRepository.findByEmailAndIsDeletedTrue(email)).thenReturn(Optional.of(deletedUser));

        // when
        UserResponseDto response = userService.recover(email);

        // then
        assertEquals(email, response.email());
        assertFalse(deletedUser.getIsDeleted());
        assertNull(deletedUser.getDeletedAt());
        assertNotNull(deletedUser.getUpdatedAt());
    }

    @Test
    void recover_삭제된_유저_복구기간_초과_예외() {
        // given
        String email = "expired@example.com";

        User expiredUser = User.builder()
                .email(email)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(40)) // 복구 불가
                .build();

        when(userRepository.findByEmailAndIsDeletedTrue(email)).thenReturn(Optional.of(expiredUser));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.recover(email);
        });

        assertEquals("Deletion recovery period expired", exception.getMessage());
    }

    @Test
    void recover_삭제된_유저_없음_예외() {
        // given
        String email = "notfound@example.com";

        when(userRepository.findByEmailAndIsDeletedTrue(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> {
            userService.recover(email);
        });
    }

    @Test
    void getUserById_존재하는_유저_조회_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .nickname("tester")
                .isDeleted(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserResponseDto result = userService.getUserById(userId);

        // then
        assertEquals("user@example.com", result.email());
        assertEquals("tester", result.nickname());
    }

    @Test
    void getUserById_존재하지_않는_유저_예외() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUser_모든_필드_수정_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .nickname("oldNick")
                .phone("01000000000")
                .profileImage("old.png")
                .isDeleted(false)
                .build();

        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                "newNick",
                "01011112222",
                "new.png"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserResponseDto result = userService.updateUser(userId, dto);

        // then
        assertEquals("newNick", user.getNickname());
        assertEquals("01011112222", user.getPhone());
        assertEquals("new.png", user.getProfileImage());
        assertNotNull(user.getUpdatedAt());
        assertEquals("newNick", result.nickname());
    }

    @Test
    void updateUser_존재하지_않는_유저_예외() {
        // given
        Long userId = 404L;
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                "newNick",
                "01011112222",
                "new.png"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateUser(userId, dto);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUser_일부_필드만_수정_성공() {
        // given
        Long userId = 2L;
        User user = User.builder()
                .id(userId)
                .nickname("oldNick")
                .phone("01000000000")
                .profileImage("old.png")
                .isDeleted(false)
                .build();

        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null,
                null,
                "updated.png"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserResponseDto result = userService.updateUser(userId, dto);

        // then
        assertEquals("oldNick", result.nickname()); // 닉네임은 그대로
        assertEquals("updated.png", result.profileImage());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void getSettings_정상_조회_성공() {
        // given
        Long userId = 1L;
        UserSettings settings = UserSettings.builder()
                .notificationEnabled(true)
                .privacyLevel(PrivacyLevel.PRIVATE)
                .build();

        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        // when
        UserSettingsDto result = userService.getSettings(userId);

        // then
        assertTrue(result.notificationEnabled());
        assertEquals(PrivacyLevel.PRIVATE, result.privacyLevel());
    }

    @Test
    void getSettings_설정_없음_예외() {
        // given
        Long userId = 99L;
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.getSettings(userId);
        });

        assertEquals("User settings not found", exception.getMessage());
    }

    @Test
    void updateSettings_정상_수정_성공() {
        // given
        Long userId = 1L;
        UserSettings settings = UserSettings.builder()
                .notificationEnabled(false)
                .privacyLevel(PrivacyLevel.PUBLIC)
                .build();

        UserSettingsUpdateDto dto = new UserSettingsUpdateDto(true, PrivacyLevel.PRIVATE);

        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        // when
        UserSettingsDto result = userService.updateSettings(userId, dto);

        // then
        assertTrue(result.notificationEnabled());
        assertEquals(PrivacyLevel.PRIVATE, result.privacyLevel());
        assertEquals(true, settings.getNotificationEnabled());
        assertEquals(PrivacyLevel.PRIVATE, settings.getPrivacyLevel());
    }

    @Test
    void updateSettings_설정_없음_예외() {
        // given
        Long userId = 77L;
        UserSettingsUpdateDto dto = new UserSettingsUpdateDto(true, PrivacyLevel.PRIVATE);

        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateSettings(userId, dto);
        });

        assertEquals("User settings not found", exception.getMessage());
    }

    @Test
    void deleteUser_정상_탈퇴_처리() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isDeleted(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        assertTrue(user.getIsDeleted());
        assertNotNull(user.getDeletedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void getPublicProfile_정상_조회_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .nickname("tester")
                .profileImage("profile.png")
                .isDeleted(false)
                .build();

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        // when
        PublicProfileDto result = userService.getPublicProfile(userId);

        // then
        assertEquals(userId, result.userId());
        assertEquals("tester", result.nickname());
        assertEquals("profile.png", result.profileImage());
    }

    @Test
    void getPublicProfile_삭제된_또는_없는_유저_예외() {
        // given
        Long userId = 999L;
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.getPublicProfile(userId);
        });

        assertEquals("User not found", exception.getMessage());
    }

}
