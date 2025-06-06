package moneybuddy.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자 계정을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    @PostMapping
    public UserResponseDto signup(@Valid @RequestBody UserSignupRequestDto requestDto) {
        return userService.signup(requestDto);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 수행하고 JWT 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = String.class)))
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        return userService.login(requestDto);
    }

    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "사용자 정보 수정", description = "ID로 사용자 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id,
                                      @Valid @RequestBody UserUpdateRequestDto requestDto) {
        return userService.updateUser(id, requestDto);
    }

    @Operation(summary = "사용자 삭제", description = "ID로 사용자 계정을 삭제(탈퇴)합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "공개 프로필 조회", description = "ID로 특정 사용자의 공개 프로필을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PublicProfileDto.class)))
    @GetMapping("/{id}/profile")
    public PublicProfileDto getPublicProfile(@PathVariable Long id) {
        return userService.getPublicProfile(id);
    }

    @Operation(summary = "사용자 설정 조회", description = "사용자의 알림 및 개인 설정 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserSettingsDto.class)))
    @GetMapping("/{user_id}/settings")
    public UserSettingsDto getUserSettings(@PathVariable("user_id") Long userId) {
        return userService.getSettings(userId);
    }

    @Operation(summary = "사용자 설정 수정", description = "사용자의 알림 및 개인 설정 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UserSettingsDto.class)))
    @PutMapping("/{user_id}/settings")
    public UserSettingsDto updateUserSettings(@PathVariable("user_id") Long userId,
                                              @Valid @RequestBody UserSettingsUpdateDto requestDto) {
        return userService.updateSettings(userId, requestDto);
    }

    @Operation(summary = "탈퇴한 계정 복구", description = "탈퇴한 지 30일 이내의 계정을 복구합니다.")
    @ApiResponse(responseCode = "200", description = "복구 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    @PostMapping("/recover")
    public UserResponseDto recoverUser(@Valid @RequestBody UserRecoverRequestDto requestDto) {
        return userService.recover(requestDto.email());
    }

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    @GetMapping("/me")
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponseDto.from(user);
    }

}
