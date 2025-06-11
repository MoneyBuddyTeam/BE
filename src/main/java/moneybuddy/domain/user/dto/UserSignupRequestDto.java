package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;

@Schema(description = "회원가입 요청 DTO")
public record UserSignupRequestDto(

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    String email,

    @Schema(description = "비밀번호", example = "1234abcd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String password,

    @Schema(description = "닉네임", example = "머니버디유저", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String nickname,

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String phone,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String profileImage,

    @Schema(description = "사용자 역할", example = "USER", defaultValue = "USER")
    UserRole role,

    @Schema(description = "로그인 방식", example = "EMAIL", defaultValue = "EMAIL")
    LoginMethod loginMethod

) {
    public static UserSignupRequestDto of(
            String email,
            String password,
            String nickname,
            String phone,
            String profileImage
    ) {
        return new UserSignupRequestDto(email, password, nickname, phone, profileImage, UserRole.USER, LoginMethod.EMAIL);
    }
}
