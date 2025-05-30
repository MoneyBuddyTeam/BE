package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 로그인 요청 DTO")
public class UserLoginRequestDto {

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;

    @Schema(description = "비밀번호", example = "1234abcd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;
}
