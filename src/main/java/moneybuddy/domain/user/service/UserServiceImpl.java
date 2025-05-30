package moneybuddy.domain.user.service;

import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.entity.UserSettings;
import moneybuddy.domain.user.repository.UserSettingsRepository;
import moneybuddy.global.enums.ErrorCode;
import moneybuddy.global.enums.PrivacyLevel;
import moneybuddy.global.enums.UserRole;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.config.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import moneybuddy.global.exception.GlobalException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSettingsRepository userSettingsRepository;

    @Override
    @Transactional
    public UserResponseDto signup(UserSignupRequestDto dto) {
        // 1. 이메일 중복 확인 및 탈퇴 계정 처리
        userRepository.findByEmail(dto.getEmail()).ifPresent(existingUser -> {
            if (existingUser.getIsDeleted()) {
                // 탈퇴 계정 복구 가능 여부 확인
                if (existingUser.getDeletedAt() != null &&
                        existingUser.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {
                    // 30일 경과 → 탈퇴 계정 완전 삭제
                    userRepository.delete(existingUser);
                } else {
                    // 복구 유도
                    throw new GlobalException(ErrorCode.USER_DELETED); // "탈퇴한 계정입니다. 복구가 가능합니다."
                }
            } else {
                // 현재 사용 중인 이메일
                throw new GlobalException(ErrorCode.USER_ALREADY_EXISTS);
            }
        });

        // 2. 전화번호 중복 확인 (isDeleted=false인 계정만 제한)
        if (dto.getPhone() != null) {
            userRepository.findByPhone(dto.getPhone()).ifPresent(user -> {
                if (!user.getIsDeleted()) {
                    throw new GlobalException(ErrorCode.PHONE_ALREADY_EXISTS); // "이미 사용 중인 전화번호입니다."
                }
            });
        }

        // 3. 닉네임 중복 확인 (탈퇴 후 30일 이내도 사용 불가)
        userRepository.findByNickname(dto.getNickname()).ifPresent(user -> {
            if (!user.getIsDeleted()) {
                throw new GlobalException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            if (user.getDeletedAt() != null &&
                    user.getDeletedAt().plusDays(30).isAfter(LocalDateTime.now())) {
                throw new GlobalException(ErrorCode.NICKNAME_ALREADY_EXISTS); // 탈퇴 30일 이내 보호
            }
        });

        // 4. 사용자 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .phone(dto.getPhone())
                .profileImage(dto.getProfileImage())
                .role(dto.getRole() != null ? dto.getRole() : UserRole.USER)
                .loginMethod(dto.getLoginMethod())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // 5. 기본 설정값 저장
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
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_LOGIN));

        if (user.getIsDeleted()) {
            throw new GlobalException(ErrorCode.USER_DELETED); // 탈퇴한 사용자 로그인 차단
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.INVALID_LOGIN);
        }

        return jwtTokenProvider.createToken(user.getId(), user.getRole().name());
    }

    @Override
    @Transactional
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (requestDto.getNickname() != null) {
            user.setNickname(requestDto.getNickname());
        }
        if (requestDto.getPhone() != null) {
            user.setPhone(requestDto.getPhone());
        }
        if (requestDto.getProfileImage() != null) {
            user.setProfileImage(requestDto.getProfileImage());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

public UserResponseDto recover(String email) {
    // 탈퇴된 유저 조회
    Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedTrue(email);

    if (optionalUser.isEmpty()) {
        // DB에 존재하지 않음 (이미 영구 삭제됐거나 애초에 존재하지 않음)
        throw new GlobalException(ErrorCode.USER_NOT_FOUND);
    }

    User user = optionalUser.get();

    // 복구 기한이 지난 경우
    if (user.getDeletedAt() != null && user.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {
        throw new GlobalException(ErrorCode.USER_DELETION_EXPIRED);
    }

    // 복구 처리
    user.setIsDeleted(false);
    user.setDeletedAt(null);
    user.setUpdatedAt(LocalDateTime.now());

    return UserResponseDto.from(user);
}


    @Override
    @Transactional
    public PublicProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        return PublicProfileDto.from(user.getId(), user.getNickname(), user.getProfileImage());
    }

    @Override
    @Transactional
    public UserSettingsDto getSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_SETTINGS_NOT_FOUND));

        return UserSettingsDto.builder()
                .notificationEnabled(settings.getNotificationEnabled())
                .privacyLevel(settings.getPrivacyLevel())
                .build();
    }

    @Override
    @Transactional
    public UserSettingsDto updateSettings(Long userId, UserSettingsUpdateDto dto) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_SETTINGS_NOT_FOUND));

        settings.setNotificationEnabled(dto.getNotificationEnabled());
        settings.setPrivacyLevel(dto.getPrivacyLevel());

        return UserSettingsDto.builder()
                .notificationEnabled(settings.getNotificationEnabled())
                .privacyLevel(settings.getPrivacyLevel())
                .build();
    }
}
