package moneybuddy.domain.advisor.dto;

import java.math.BigDecimal;
import java.util.List;
import moneybuddy.domain.advisor.entity.Advisor;

public record AdvisorListResponse(
    Long id,
    String name,
    String bio,
    BigDecimal price,
    Boolean isOnline,
    Boolean available,
    List<String> categories
) {

  public static AdvisorListResponse from(Advisor advisor) {
    List<String> categories = advisor.getAdvisorTags().stream()
        .map(advisorTag -> advisorTag.getCategory().getName())
        .distinct()
        .toList();

    return new AdvisorListResponse(
        advisor.getId(),
        advisor.getName(),
        truncateBio(advisor.getBio()),
        advisor.getPrice(),
        advisor.getIsOnline(),
        advisor.getAvailable(),
        categories
    );
  }

  /**
   * 목록 표시용으로 bio를 100자로 제한
   */
  private static String truncateBio(String bio) {
    if (bio == null || bio.length() <= 100) {
      return bio;
    }
    return bio.substring(0, 100) + "...";
  }
}
