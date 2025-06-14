package moneybuddy.domain.advisor.dto;

import moneybuddy.domain.advisor.entity.Category;

public record CategoryResponse(
    Long id,
    String name,
    String type,
    String typeDisplayName
) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getType().name(),
        getTypeDisplayName(category.getType())
    );
  }

  /**
   * 카테고리 타입의 한글 표시명 반환
   */
  private static String getTypeDisplayName(Category.CategoryType type) {
    return switch (type) {
      case SPENDING -> "소비";
      case SAVINGS -> "저축";
      case INVESTMENT -> "투자";
      case DEBT -> "부채";
      case ETC -> "기타";
    };
  }
}
