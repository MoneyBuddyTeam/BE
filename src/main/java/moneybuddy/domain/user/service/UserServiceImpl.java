package moneybuddy.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.config.JwtTokenProvider;
import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.entity.UserSettings;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.domain.user.repository.UserSettingsRepository;
import moneybuddy.global.enums.PrivacyLevel;
import moneybuddy.global.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSettingsRepository userSettingsRepository;

    @Override
    @Transactional
    public UserResponseDto signup(UserSignupRequestDto dto) {
        validateEmailUniquenessOrRecover(dto.email());
        validatePhoneUniqueness(dto.phone());
        validateNicknameUniqueness(dto.nickname());

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .nickname(dto.nickname())
                .phone(dto.phone())
                .profileImage(dto.profileImage())
                .role(dto.role() != null ? dto.role() : UserRole.USER)
                .loginMethod(dto.loginMethod())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        UserSettings settings = UserSettings.builder()
                .user(user)
                .notificationEnabled(true)
                .privacyLevel(PrivacyLevel.PRIVATE)
                .build();

        userSettingsRepository.save(settings);

        return UserResponseDto.from(user);
    }

    @Override
    public String login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new NoSuchElementException("Invalid login: user not found"));

        if (user.getIsDeleted()) {
            log.warn("Login attempt for deleted user: {}", dto.email());
            throw new IllegalStateException("Account is deleted");
        }

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.warn("Login password mismatch for user: {}", dto.email());
            throw new IllegalArgumentException("Invalid password");
        }

        return jwtTokenProvider.createToken(user.getId(), user.getRole().name());
    }

    @Override
    @Transactional
    public UserResponseDto getUserById(Long userId) {
        return UserResponseDto.from(getUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto dto) {
        User user = getUserOrThrow(userId);

        if (dto.nickname() != null) user.setNickname(dto.nickname());
        if (dto.phone() != null) user.setPhone(dto.phone());
        if (dto.profileImage() != null) user.setProfileImage(dto.profileImage());

        user.setUpdatedAt(LocalDateTime.now());
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserOrThrow(userId);
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    public UserResponseDto recover(String email) {
        User user = getDeletedUserOrThrow(email);

        if (isOverDeletionPeriod(user.getDeletedAt())) {
            log.info("Attempted to recover expired deleted user: {}", email);
            throw new IllegalStateException("Deletion recovery period expired");
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);
        user.setUpdatedAt(LocalDateTime.now());

        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public PublicProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return PublicProfileDto.from(user.getId(), user.getNickname(), user.getProfileImage());
    }

    @Override
    @Transactional
    public UserSettingsDto getSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User settings not found"));

        return new UserSettingsDto(settings.getNotificationEnabled(), settings.getPrivacyLevel());
    }

    @Override
    @Transactional
    public UserSettingsDto updateSettings(Long userId, UserSettingsUpdateDto dto) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User settings not found"));

        settings.setNotificationEnabled(dto.notificationEnabled());
        settings.setPrivacyLevel(dto.privacyLevel());

        return new UserSettingsDto(settings.getNotificationEnabled(), settings.getPrivacyLevel());
    }

    // ======================= 헬퍼 메서드 =======================

    private void validateEmailUniquenessOrRecover(String email) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (existingUser.getIsDeleted()) {
                if (isOverDeletionPeriod(existingUser.getDeletedAt())) {
                    userRepository.delete(existingUser);
                } else {
                    log.warn("Signup attempted for deleted user within recovery period: {}", email);
                    throw new IllegalStateException("Deleted user cannot be reused yet");
                }
            } else {
                log.warn("Signup attempted for already registered email: {}", email);
                throw new IllegalStateException("Email is already registered");
            }
        });
    }

    private void validatePhoneUniqueness(String phone) {
        if (phone != null) {
            userRepository.findByPhone(phone).ifPresent(user -> {
                if (!user.getIsDeleted()) {
                    log.warn("Signup attempted with already used phone number: {}", phone);
                    throw new IllegalStateException("Phone number is already in use");
                }
            });
        }
    }

    private void validateNicknameUniqueness(String nickname) {
        userRepository.findByNickname(nickname).ifPresent(user -> {
            if (!user.getIsDeleted()) {
                log.warn("Signup attempted with duplicate nickname: {}", nickname);
                throw new IllegalStateException("Nickname is already in use");
            }
            if (!isOverDeletionPeriod(user.getDeletedAt())) {
                log.warn("Signup attempted with nickname in deletion recovery period: {}", nickname);
                throw new IllegalStateException("Nickname is protected during recovery period");
            }
        });
    }

    private boolean isOverDeletionPeriod(LocalDateTime deletedAt) {
        return deletedAt != null && deletedAt.plusDays(30).isBefore(LocalDateTime.now());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private User getDeletedUserOrThrow(String email) {
        return userRepository.findByEmailAndIsDeletedTrue(email)
                .orElseThrow(() -> new NoSuchElementException("Deleted user not found"));
    }
}
