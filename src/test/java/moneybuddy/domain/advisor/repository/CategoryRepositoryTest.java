package moneybuddy.domain.advisor.repository;

import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestConfig.class})
@DisplayName("CategoryRepository 통합 테스트")
class CategoryRepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  @DisplayName("카테고리 타입별 조회 - 투자")
  void findByType_Investment() {
    // when
    List<Category> result = categoryRepository.findByType(Category.CategoryType.INVESTMENT);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("투자전략");
    assertThat(result.get(0).getType()).isEqualTo(Category.CategoryType.INVESTMENT);
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 저축")
  void findByType_Savings() {
    // when
    List<Category> result = categoryRepository.findByType(Category.CategoryType.SAVINGS);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("저축계획");
    assertThat(result.get(0).getType()).isEqualTo(Category.CategoryType.SAVINGS);
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 소비")
  void findByType_Spending() {
    // when
    List<Category> result = categoryRepository.findByType(Category.CategoryType.SPENDING);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("소비관리");
    assertThat(result.get(0).getType()).isEqualTo(Category.CategoryType.SPENDING);
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 부채")
  void findByType_Debt() {
    // when
    List<Category> result = categoryRepository.findByType(Category.CategoryType.DEBT);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("부채관리");
    assertThat(result.get(0).getType()).isEqualTo(Category.CategoryType.DEBT);
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 기타")
  void findByType_Etc() {
    // when
    List<Category> result = categoryRepository.findByType(Category.CategoryType.ETC);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("기타상담");
    assertThat(result.get(0).getType()).isEqualTo(Category.CategoryType.ETC);
  }

  @Test
  @DisplayName("카테고리 이름으로 조회 - 성공")
  void findByName_Success() {
    // when
    Optional<Category> result = categoryRepository.findByName("투자전략");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("투자전략");
    assertThat(result.get().getType()).isEqualTo(Category.CategoryType.INVESTMENT);
  }

  @Test
  @DisplayName("카테고리 이름으로 조회 - 존재하지 않는 경우")
  void findByName_NotFound() {
    // when
    Optional<Category> result = categoryRepository.findByName("존재하지않는카테고리");

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("카테고리 이름 존재 여부 확인 - 존재함")
  void existsByName_Exists() {
    // when & then
    assertThat(categoryRepository.existsByName("투자전략")).isTrue();
    assertThat(categoryRepository.existsByName("저축계획")).isTrue();
    assertThat(categoryRepository.existsByName("소비관리")).isTrue();
    assertThat(categoryRepository.existsByName("부채관리")).isTrue();
    assertThat(categoryRepository.existsByName("기타상담")).isTrue();
  }

  @Test
  @DisplayName("카테고리 이름 존재 여부 확인 - 존재하지 않음")
  void existsByName_NotExists() {
    // when & then
    assertThat(categoryRepository.existsByName("존재하지않는카테고리")).isFalse();
    assertThat(categoryRepository.existsByName("")).isFalse();
  }

  @Test
  @DisplayName("모든 카테고리를 이름 순으로 조회")
  void findAllByOrderByNameAsc() {
    // when
    List<Category> result = categoryRepository.findAllByOrderByNameAsc();

    // then
    assertThat(result).hasSize(5);

    // 이름순 정렬 확인 (가나다 순)
    assertThat(result)
        .extracting(Category::getName)
        .containsExactly("기타상담", "부채관리", "소비관리", "저축계획", "투자전략");
  }

  @Test
  @DisplayName("전체 카테고리 조회")
  void findAll() {
    // when
    List<Category> result = categoryRepository.findAll();

    // then
    assertThat(result).hasSize(5);

    // 모든 카테고리 타입이 포함되어 있는지 확인
    assertThat(result)
        .extracting(Category::getType)
        .containsExactlyInAnyOrder(
            Category.CategoryType.SPENDING,
            Category.CategoryType.SAVINGS,
            Category.CategoryType.INVESTMENT,
            Category.CategoryType.DEBT,
            Category.CategoryType.ETC
        );
  }

  @Test
  @DisplayName("ID로 카테고리 조회")
  void findById() {
    // when
    Optional<Category> result = categoryRepository.findById(1L);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
    assertThat(result.get().getName()).isEqualTo("소비관리");
  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 조회")
  void findById_NotFound() {
    // when
    Optional<Category> result = categoryRepository.findById(999L);

    // then
    assertThat(result).isEmpty();
  }
}
