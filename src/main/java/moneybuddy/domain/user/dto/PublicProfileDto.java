package moneybuddy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 공개 프로필 응답 DTO")
public class PublicProfileDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "마이머니버디")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
    private String profileImage;

    public static PublicProfileDto from(Long userId, String nickname, String profileImage) {
        return PublicProfileDto.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
}
