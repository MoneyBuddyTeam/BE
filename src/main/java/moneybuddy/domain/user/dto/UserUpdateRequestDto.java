package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 수정 요청 DTO")
public record UserUpdateRequestDto(

    @Schema(description = "닉네임", example = "업데이트된유저", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String nickname,

    @Schema(description = "전화번호", example = "010-9876-5432", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String phone,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/updated-image.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String profileImage

) {}
