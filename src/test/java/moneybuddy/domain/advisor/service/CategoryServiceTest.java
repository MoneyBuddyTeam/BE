package moneybuddy.domain.advisor.service;

import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.dto.CategoryResponse;
import moneybuddy.domain.advisor.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@DisplayName("CategoryService 통합 테스트")
class CategoryServiceTest {

  @Autowired
  private CategoryService categoryService;

  @Test
  @DisplayName("모든 카테고리 조회 - 성공")
  void getAllCategories_Success() {
    // when
    List<CategoryResponse> result = categoryService.getAllCategories();

    // then
    assertThat(result).hasSize(5); // 테스트 데이터: 5개 카테고리

    // 이름순 정렬 확인 (가나다 순)
    assertThat(result)
        .extracting(CategoryResponse::name)
        .containsExactly("기타상담", "부채관리", "소비관리", "저축계획", "투자전략");

    // 각 카테고리의 타입과 표시명 확인
    CategoryResponse spendingCategory = result.stream()
        .filter(cat -> cat.name().equals("소비관리"))
        .findFirst()
        .orElseThrow();

    assertThat(spendingCategory.type()).isEqualTo("SPENDING");
    assertThat(spendingCategory.typeDisplayName()).isEqualTo("소비");
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 투자 카테고리")
  void getCategoriesByType_Investment_Success() {
    // when
    List<CategoryResponse> result = categoryService.getCategoriesByType(Category.CategoryType.INVESTMENT);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("투자전략");
    assertThat(result.get(0).type()).isEqualTo("INVESTMENT");
    assertThat(result.get(0).typeDisplayName()).isEqualTo("투자");
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 저축 카테고리")
  void getCategoriesByType_Savings_Success() {
    // when
    List<CategoryResponse> result = categoryService.getCategoriesByType(Category.CategoryType.SAVINGS);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("저축계획");
    assertThat(result.get(0).type()).isEqualTo("SAVINGS");
    assertThat(result.get(0).typeDisplayName()).isEqualTo("저축");
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 소비 카테고리")
  void getCategoriesByType_Spending_Success() {
    // when
    List<CategoryResponse> result = categoryService.getCategoriesByType(Category.CategoryType.SPENDING);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("소비관리");
    assertThat(result.get(0).type()).isEqualTo("SPENDING");
    assertThat(result.get(0).typeDisplayName()).isEqualTo("소비");
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 부채 카테고리")
  void getCategoriesByType_Debt_Success() {
    // when
    List<CategoryResponse> result = categoryService.getCategoriesByType(Category.CategoryType.DEBT);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("부채관리");
    assertThat(result.get(0).type()).isEqualTo("DEBT");
    assertThat(result.get(0).typeDisplayName()).isEqualTo("부채");
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 기타 카테고리")
  void getCategoriesByType_Etc_Success() {
    // when
    List<CategoryResponse> result = categoryService.getCategoriesByType(Category.CategoryType.ETC);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("기타상담");
    assertThat(result.get(0).type()).isEqualTo("ETC");
    assertThat(result.get(0).typeDisplayName()).isEqualTo("기타");
  }

  @Test
  @DisplayName("카테고리 ID로 조회 - 성공")
  void getCategoryById_Success() {
    // given
    Long categoryId = 1L; // 소비관리

    // when
    CategoryResponse result = categoryService.getCategoryById(categoryId);

    // then
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("소비관리");
    assertThat(result.type()).isEqualTo("SPENDING");
    assertThat(result.typeDisplayName()).isEqualTo("소비");
  }

  @Test
  @DisplayName("카테고리 ID로 조회 - 투자전략 카테고리")
  void getCategoryById_Investment_Success() {
    // given
    Long categoryId = 3L; // 투자전략

    // when
    CategoryResponse result = categoryService.getCategoryById(categoryId);

    // then
    assertThat(result.id()).isEqualTo(3L);
    assertThat(result.name()).isEqualTo("투자전략");
    assertThat(result.type()).isEqualTo("INVESTMENT");
    assertThat(result.typeDisplayName()).isEqualTo("투자");
  }

  @Test
  @DisplayName("카테고리 ID로 조회 - 존재하지 않는 카테고리")
  void getCategoryById_NotFound_ThrowsException() {
    // given
    Long categoryId = 999L;

    // when & then
    assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 카테고리입니다.");
  }

  @Test
  @DisplayName("모든 카테고리 타입 표시명 검증")
  void getAllCategories_TypeDisplayNames_Success() {
    // when
    List<CategoryResponse> result = categoryService.getAllCategories();

    // then - 각 타입별 표시명이 올바른지 확인
    result.forEach(category -> {
      switch (category.type()) {
        case "SPENDING" -> assertThat(category.typeDisplayName()).isEqualTo("소비");
        case "SAVINGS" -> assertThat(category.typeDisplayName()).isEqualTo("저축");
        case "INVESTMENT" -> assertThat(category.typeDisplayName()).isEqualTo("투자");
        case "DEBT" -> assertThat(category.typeDisplayName()).isEqualTo("부채");
        case "ETC" -> assertThat(category.typeDisplayName()).isEqualTo("기타");
        default -> throw new AssertionError("알 수 없는 카테고리 타입: " + category.type());
      }
    });
  }

  @Test
  @DisplayName("카테고리 ID 존재 여부 검증")
  void getCategoryById_AllTestData_Success() {
    // given - 테스트 데이터의 모든 카테고리 ID
    List<Long> categoryIds = List.of(1L, 2L, 3L, 4L, 5L);

    // when & then - 모든 카테고리가 정상 조회되는지 확인
    for (Long categoryId : categoryIds) {
      CategoryResponse result = categoryService.getCategoryById(categoryId);
      assertThat(result.id()).isEqualTo(categoryId);
      assertThat(result.name()).isNotNull();
      assertThat(result.type()).isNotNull();
      assertThat(result.typeDisplayName()).isNotNull();
    }
  }
}
