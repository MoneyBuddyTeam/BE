package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import moneybuddy.domain.user.entity.User;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;

@Schema(description = "사용자 정보 응답 DTO")
public record UserResponseDto(

    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일 주소", example = "user@example.com")
    String email,

    @Schema(description = "닉네임", example = "머니버디유저")
    String nickname,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    String profileImage,

    @Schema(description = "사용자 역할", example = "USER")
    UserRole role,

    @Schema(description = "로그인 방식", example = "EMAIL")
    LoginMethod loginMethod

) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getProfileImage(),
                user.getRole(),
                user.getLoginMethod()
        );
    }
}
