package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moneybuddy.domain.user.entity.User;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;

@Getter
@Builder
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "머니버디유저")
    private String nickname;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    private String profileImage;

    @Schema(description = "사용자 역할", example = "USER")
    private UserRole role;

    @Schema(description = "로그인 방식", example = "EMAIL")
    private LoginMethod loginMethod;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .loginMethod(user.getLoginMethod())
                .build();
    }
}
