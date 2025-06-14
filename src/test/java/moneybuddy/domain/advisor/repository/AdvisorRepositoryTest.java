package moneybuddy.domain.advisor.repository;

import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.entity.Advisor;
import moneybuddy.domain.advisor.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestConfig.class})
@DisplayName("AdvisorRepository 통합 테스트")
class AdvisorRepositoryTest {

  @Autowired
  private AdvisorRepository advisorRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  @DisplayName("활성화된 전문가 조회")
  void findByAvailableTrue() {
    // given
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByAvailableTrue(pageable);

    // then
    assertThat(result.getContent()).hasSize(3); // 테스트 데이터에서 available=true인 전문가 3명
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "박저축", "이부채");
  }

  @Test
  @DisplayName("온라인 상태인 활성 전문가 조회")
  void findByIsOnlineTrueAndAvailableTrue() {
    // given
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByIsOnlineTrueAndAvailableTrue(pageable);

    // then
    assertThat(result.getContent()).hasSize(2); // 김투자, 이부채 (isOnline=true & available=true)
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "이부채");

    // 모든 결과가 온라인 상태인지 확인
    assertThat(result.getContent())
        .extracting(Advisor::getIsOnline)
        .allMatch(isOnline -> isOnline);
  }

  @Test
  @DisplayName("사용자 ID로 전문가 조회")
  void findByUserId() {
    // when
    Optional<Advisor> result = advisorRepository.findByUserId(101L);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("김투자");
    assertThat(result.get().getUserId()).isEqualTo(101L);
  }

  @Test
  @DisplayName("존재하지 않는 사용자 ID로 조회")
  void findByUserId_NotFound() {
    // when
    Optional<Advisor> result = advisorRepository.findByUserId(999L);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("카테고리별 활성 전문가 조회")
  void findByCategoryIdAndAvailableTrue() {
    // given - 투자전략 카테고리 ID 동적 조회
    Category investmentCategory = categoryRepository.findByName("투자전략").orElseThrow();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByCategoryIdAndAvailableTrue(investmentCategory.getId(), pageable);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("김투자");
  }

  @Test
  @DisplayName("여러 카테고리에 해당하는 활성 전문가 조회")
  void findByCategoryIdsAndAvailableTrue() {
    // given - 저축계획, 투자전략 카테고리 ID 동적 조회
    Category savingsCategory = categoryRepository.findByName("저축계획").orElseThrow();
    Category investmentCategory = categoryRepository.findByName("투자전략").orElseThrow();
    List<Long> categoryIds = List.of(savingsCategory.getId(), investmentCategory.getId());
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByCategoryIdsAndAvailableTrue(categoryIds, pageable);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "박저축");
  }

  @Test
  @DisplayName("가격 범위로 활성 전문가 조회")
  void findByPriceBetweenAndAvailableTrue() {
    // given
    BigDecimal minPrice = new BigDecimal("30000");
    BigDecimal maxPrice = new BigDecimal("50000");
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByPriceBetweenAndAvailableTrue(minPrice, maxPrice, pageable);

    // then
    assertThat(result.getContent()).hasSize(3); // 박저축(30000), 이부채(40000), 김투자(50000)
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "박저축", "이부채");
  }

  @Test
  @DisplayName("전문가 이름으로 검색")
  void findByNameContainingIgnoreCaseAndAvailableTrue() {
    // given
    String keyword = "김";
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByNameContainingIgnoreCaseAndAvailableTrue(keyword, pageable);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("김투자");
  }

  @Test
  @DisplayName("키워드로 전문가 검색 (이름)")
  void findByKeywordAndAvailableTrue_Name() {
    // given
    String keyword = "김투자";
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByKeywordAndAvailableTrue(keyword, pageable);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("김투자");
  }

  @Test
  @DisplayName("키워드로 전문가 검색 (자기소개)")
  void findByKeywordAndAvailableTrue_Bio() {
    // given
    String keyword = "투자";
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByKeywordAndAvailableTrue(keyword, pageable);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("김투자");
    assertThat(result.getContent().get(0).getBio()).contains("투자");
  }

  @Test
  @DisplayName("추천 전문가 조회")
  void findRecommendedAdvisors() {
    // given - 김투자 ID 동적 조회
    Advisor kimInvestor = advisorRepository.findByUserId(101L).orElseThrow();
    Pageable pageable = PageRequest.of(0, 4);

    // when
    List<Advisor> result = advisorRepository.findRecommendedAdvisors(kimInvestor.getId(), pageable);

    // then - 김투자와 동일한 카테고리를 가진 다른 전문가들
    assertThat(result).isNotEmpty();
    assertThat(result).extracting(Advisor::getName).doesNotContain("김투자");
    // 김투자는 투자전략, 기타상담 카테고리를 가지고 있고, 박저축도 기타상담을 가지므로 추천되어야 함
    assertThat(result).extracting(Advisor::getName).contains("박저축");
  }

  @Test
  @DisplayName("추천 전문가 조회 - 공통 카테고리가 없는 경우")
  void findRecommendedAdvisors_NoCommonCategory() {
    // given - 이부채 ID 동적 조회 (부채관리 카테고리만 가짐)
    Advisor debtAdvisor = advisorRepository.findByUserId(103L).orElseThrow();
    Pageable pageable = PageRequest.of(0, 4);

    // when
    List<Advisor> result = advisorRepository.findRecommendedAdvisors(debtAdvisor.getId(), pageable);

    // then - 부채관리 카테고리만 가진 전문가는 이부채뿐이므로 추천 전문가 없음
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("사용자 ID로 전문가 존재 여부 확인")
  void existsByUserId() {
    // when & then
    assertThat(advisorRepository.existsByUserId(101L)).isTrue(); // 김투자
    assertThat(advisorRepository.existsByUserId(102L)).isTrue(); // 박저축
    assertThat(advisorRepository.existsByUserId(103L)).isTrue(); // 이부채
    assertThat(advisorRepository.existsByUserId(104L)).isTrue(); // 최소비
    assertThat(advisorRepository.existsByUserId(999L)).isFalse(); // 존재하지 않음
  }

  @Test
  @DisplayName("가격 범위 조회 - 최소 가격만 지정")
  void findByPriceBetweenAndAvailableTrue_MinPriceOnly() {
    // given
    BigDecimal minPrice = new BigDecimal("40000");
    BigDecimal maxPrice = new BigDecimal("999999"); // 매우 큰 값
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByPriceBetweenAndAvailableTrue(minPrice, maxPrice, pageable);

    // then
    assertThat(result.getContent()).hasSize(2); // 이부채(40000), 김투자(50000)
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "이부채");
  }

  @Test
  @DisplayName("가격 범위 조회 - 범위에 해당하는 전문가 없음")
  void findByPriceBetweenAndAvailableTrue_NoMatch() {
    // given
    BigDecimal minPrice = new BigDecimal("60000");
    BigDecimal maxPrice = new BigDecimal("70000");
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByPriceBetweenAndAvailableTrue(minPrice, maxPrice, pageable);

    // then
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("비활성화된 전문가는 조회되지 않음")
  void findByAvailableTrue_ExcludesInactive() {
    // given
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByAvailableTrue(pageable);

    // then - 최소비(available=false)는 조회되지 않아야 함
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .doesNotContain("최소비");
    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  @DisplayName("오프라인 전문가는 온라인 전용 조회에서 제외")
  void findByIsOnlineTrueAndAvailableTrue_ExcludesOffline() {
    // given
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByIsOnlineTrueAndAvailableTrue(pageable);

    // then - 박저축(isOnline=false)은 조회되지 않아야 함
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .doesNotContain("박저축");
    assertThat(result.getContent())
        .extracting(Advisor::getName)
        .contains("김투자", "이부채");
  }

  @Test
  @DisplayName("빈 키워드로 검색")
  void findByKeywordAndAvailableTrue_EmptyKeyword() {
    // given
    String keyword = "";
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByKeywordAndAvailableTrue(keyword, pageable);

    // then - 빈 키워드는 모든 활성 전문가를 반환해야 함
    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  @DisplayName("존재하지 않는 카테고리로 조회")
  void findByCategoryIdAndAvailableTrue_NotFound() {
    // given
    Long nonExistentCategoryId = 999L;
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Advisor> result = advisorRepository.findByCategoryIdAndAvailableTrue(nonExistentCategoryId, pageable);

    // then
    assertThat(result.getContent()).isEmpty();
  }
}
