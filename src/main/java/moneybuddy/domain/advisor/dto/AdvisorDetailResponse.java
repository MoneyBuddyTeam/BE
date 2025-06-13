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
}
