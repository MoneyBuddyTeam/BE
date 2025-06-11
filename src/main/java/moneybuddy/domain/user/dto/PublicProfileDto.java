package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 공개 프로필 응답 DTO")
public record PublicProfileDto(

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "닉네임", example = "마이머니버디")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
    String profileImage

) {
    public static PublicProfileDto from(Long userId, String nickname, String profileImage) {
        return new PublicProfileDto(userId, nickname, profileImage);
    }
}
