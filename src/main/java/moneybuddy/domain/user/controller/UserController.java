package moneybuddy.domain.user.controller;

import moneybuddy.domain.user.dto.*;
import moneybuddy.domain.user.service.UserService;
import moneybuddy.global.entity.ApiErrorSchema;
import moneybuddy.global.entity.CommonResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorSchema.class))),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복"),
            @ApiResponse(responseCode = "403", description = "탈퇴한 계정",
                    content = @Content(schema = @Schema(implementation = ApiErrorSchema.class)))

    })
    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> signup(
            @Valid @RequestBody UserSignupRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.signup(requestDto)));
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 수행하고 JWT 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 로그인 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorSchema.class))),
            @ApiResponse(responseCode = "403", description = "탈퇴한 계정",
                    content = @Content(schema = @Schema(implementation = ApiErrorSchema.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<String>> login(
            @Valid @RequestBody UserLoginRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.login(requestDto)));
    }

    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorSchema.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(userService.getUserById(id)));
    }

    @Operation(summary = "사용자 정보 수정", description = "ID로 사용자 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.updateUser(id, requestDto)));
    }

    @Operation(summary = "사용자 삭제", description = "ID로 사용자 계정을 삭제(탈퇴)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "공개 프로필 조회", description = "ID로 특정 사용자의 공개 프로필을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PublicProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping("/{id}/profile")
    public ResponseEntity<CommonResponse<PublicProfileDto>> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(userService.getPublicProfile(id)));
    }

    @Operation(summary = "사용자 설정 조회", description = "사용자의 알림 및 개인 설정 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserSettingsDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 설정 없음")
    })
    @GetMapping("/{user_id}/settings")
    public ResponseEntity<CommonResponse<UserSettingsDto>> getUserSettings(@PathVariable("user_id") Long userId) {
        return ResponseEntity.ok(CommonResponse.success(userService.getSettings(userId)));
    }

    @Operation(summary = "사용자 설정 수정", description = "사용자의 알림 및 개인 설정 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserSettingsDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 설정 없음")
    })
    @PutMapping("/{user_id}/settings")
    public ResponseEntity<CommonResponse<UserSettingsDto>> updateUserSettings(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody UserSettingsUpdateDto requestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.updateSettings(userId, requestDto)));
    }

        @Operation(
        summary = "탈퇴한 계정 복구",
        description = "탈퇴한 지 30일 이내의 계정을 복구합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "복구 성공",
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "410", description = "복구 기간 만료")
        }
    )
    @PostMapping("/recover")
    public ResponseEntity<CommonResponse<UserResponseDto>> recoverUser(
            @Valid @RequestBody UserRecoverRequestDto requestDto) {

        UserResponseDto responseDto = userService.recover(requestDto.getEmail());
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }
}
