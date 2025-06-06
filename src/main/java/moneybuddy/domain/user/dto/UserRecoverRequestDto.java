package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사용자 계정 복구 요청 DTO")
public record UserRecoverRequestDto(

    @Schema(description = "복구할 사용자 이메일", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    String email

) {}
