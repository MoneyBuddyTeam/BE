package moneybuddy.domain.user.service;

import moneybuddy.domain.user.dto.*;

public interface UserService {
    // 회원가입
    UserResponseDto signup(UserSignupRequestDto requestDto);

    // 로그인
    String login(UserLoginRequestDto requestDto);

    // 특정 사용자 정보 조회
    UserResponseDto getUserById(Long userId);

    // 사용자 정보 수정
    UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto);

    // 사용자 계정 삭제 (탈퇴)
    void deleteUser(Long userId);

    // 공개 프로필 조회
    PublicProfileDto getPublicProfile(Long userId);

    // 설정 정보 조회
    UserSettingsDto getSettings(Long userId);

    // 설정 정보 수정
    UserSettingsDto updateSettings(Long userId, UserSettingsUpdateDto requestDto);

    UserResponseDto recover(String email);
}
