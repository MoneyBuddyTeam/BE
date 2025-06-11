package moneybuddy.domain.advisor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import moneybuddy.domain.advisor.entity.Advisor;

public record AdvisorDetailResponse(
    Long id,
    Long userId,
    String name,
    String bio,
    String certificationFile,
    BigDecimal price,
    Boolean isOnline,
    Boolean available,
    LocalDateTime createdAt,
    List<String> categories,
    List<RecommendedAdvisorResponse> recommendedAdvisors
) {

  public static AdvisorDetailResponse from(Advisor advisor, List<Advisor> recommendedAdvisors) {
    List<String> categories = advisor.getAdvisorTags().stream()
        .map(advisorTag -> advisorTag.getCategory().getName())
        .distinct()
        .toList();

    List<RecommendedAdvisorResponse> recommended = recommendedAdvisors.stream()
        .map(RecommendedAdvisorResponse::from)
        .toList();

    return new AdvisorDetailResponse(
        advisor.getId(),
        advisor.getUserId(),
        advisor.getName(),
        advisor.getBio(),
        advisor.getCertificationFile(),
        advisor.getPrice(),
        advisor.getIsOnline(),
        advisor.getAvailable(),
        advisor.getCreatedAt(),
        categories,
        recommended
    );
  }

  /**
   * 추천 전문가 정보 (간단한 정보만)
   */
  private record RecommendedAdvisorResponse(
      Long id,
      String name,
      String bio,
      BigDecimal price,
      Boolean isOnline
  ) {

    public static RecommendedAdvisorResponse from(Advisor advisor) {
      return new RecommendedAdvisorResponse(
          advisor.getId(),
          advisor.getName(),
          truncateBio(advisor.getBio()),
          advisor.getPrice(),
          advisor.getIsOnline()
      );
    }

    private static String truncateBio(String bio) {
      if (bio == null || bio.length() <= 50) {
        return bio;
      }
      return bio.substring(0, 50) + "...";
    }
  }
}
