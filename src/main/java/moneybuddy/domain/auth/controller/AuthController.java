package moneybuddy.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import moneybuddy.domain.auth.entity.RefreshToken;
import moneybuddy.domain.auth.repository.RefreshTokenRepository;
import moneybuddy.domain.auth.service.AuthService;
import moneybuddy.domain.auth.service.OAuthUnlinkService;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.util.CookieUtil;
import moneybuddy.util.JwtTokenProvider;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final OAuthUnlinkService oAuthUnlinkService;
    private final CookieUtil cookieUtil;

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token이 유효할 경우 새로운 Access Token을 쿠키로 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 완료"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 누락 또는 만료"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {

        String refreshToken = cookieUtil.getTokenFromCookie(request, "refresh_token");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body("Refresh Token 누락됨");
        }

        RefreshToken tokenRecord = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token"));

        if (tokenRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body("Refresh Token 만료됨");
        }

        User user = userRepository.findById(tokenRecord.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        ResponseCookie newAccessCookie = ResponseCookie.from("token", newAccessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 15) // 15분
                .build();

        response.setHeader("Set-Cookie", newAccessCookie.toString());
        return ResponseEntity.ok().body("Access Token 재발급 완료");
    }

    @Operation(
            summary = "로그아웃",
            description = "사용자의 Refresh Token을 삭제하고 쿠키에서 Access Token을 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/logout")
    public String logout(
            @Parameter(description = "로그인된 사용자 정보", hidden = true) @AuthenticationPrincipal User user,
            @Parameter(hidden = true) HttpServletResponse response) {
        authService.logout(user.getId(), response);
        return "로그아웃 완료";
    }

    @Operation(
            summary = "OAuth2 소셜 연동 해제",
            description = "현재 로그인한 사용자의 소셜 로그인 연동을 해제합니다. 연동 해제 후 loginMethod는 EMAIL로 전환되며, 비밀번호가 없을 경우 설정을 유도합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연동 해제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "복호화 실패 또는 외부 API 오류")
    })
    @DeleteMapping("/unlink")
    public ResponseEntity<String> unlink(@AuthenticationPrincipal User user) {
        oAuthUnlinkService.unlink(user);
        return ResponseEntity.ok("소셜 연동이 해제되었습니다.");
    }

}
