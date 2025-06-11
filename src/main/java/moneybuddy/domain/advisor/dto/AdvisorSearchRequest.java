package moneybuddy.domain.advisor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record AdvisorSearchRequest(
    @PositiveOrZero(message = "페이지 번호는 0 이상이어야 합니다.")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    Integer size,

    String sortBy,          // 정렬 기준: price_asc, price_desc, name, created_at, default
    String keyword,         // 검색 키워드 (이름, 자기소개)
    Long categoryId,        // 단일 카테고리 ID
    List<Long> categoryIds, // 복수 카테고리 IDs
    BigDecimal minPrice,    // 최소 가격
    BigDecimal maxPrice,    // 최대 가격
    Boolean onlineOnly      // 온라인 상태 전문가만 조회
) {

  public AdvisorSearchRequest {
    if (page == null) page = 0;
    if (size == null) size = 20;
    if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "default";
  }

  public Integer page() {
    return page == null ? 0 : page;
  }

  public Integer size() {
    return size == null ? 20 : Math.min(size, 100);
  }

  public String sortBy() {
    return sortBy == null ? "default" : sortBy;
  }
}
