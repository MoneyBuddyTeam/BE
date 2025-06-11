package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRecoverRequestDto {

    @NotBlank
    @Email
    @Schema(description = "복구할 사용자 이메일", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
