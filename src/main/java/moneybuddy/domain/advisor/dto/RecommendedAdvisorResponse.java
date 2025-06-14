package moneybuddy.domain.advisor.dto;

import java.math.BigDecimal;
import moneybuddy.domain.advisor.entity.Advisor;

public record RecommendedAdvisorResponse(
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

  /**
   * 추천 전문가용으로 bio를 50자로 제한
   */
  private static String truncateBio(String bio) {
    if (bio == null || bio.length() <= 50) {
      return bio;
    }
    return bio.substring(0, 50) + "...";
  }
}
