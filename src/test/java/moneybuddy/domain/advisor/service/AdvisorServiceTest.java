package moneybuddy.domain.advisor.service;

import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.dto.AdvisorDetailResponse;
import moneybuddy.domain.advisor.dto.AdvisorListResponse;
import moneybuddy.domain.advisor.dto.AdvisorSearchRequest;
import moneybuddy.domain.advisor.dto.CategoryResponse;
import moneybuddy.domain.advisor.entity.Advisor;
import moneybuddy.domain.advisor.repository.AdvisorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@DisplayName("AdvisorService 통합 테스트")
class AdvisorServiceTest {

  @Autowired
  private AdvisorService advisorService;

  @Autowired
  private AdvisorRepository advisorRepository;

  @Autowired
  private CategoryService categoryService;

  @Test
  @DisplayName("전문가 목록 조회 - 기본 조회")
  void getAdvisorList_Default_Success() {
    // given
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, null, null, null, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(3); // available=true인 전문가 3명
    assertThat(result.getContent())
        .extracting(AdvisorListResponse::name)
        .contains("김투자", "박저축", "이부채");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 키워드 검색 (이름)")
  void getAdvisorList_WithKeyword_Name_Success() {
    // given
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, "김투자", null, null, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("김투자");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 키워드 검색 (자기소개)")
  void getAdvisorList_WithKeyword_Bio_Success() {
    // given
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, "투자", null, null, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("김투자");
    assertThat(result.getContent().get(0).bio()).contains("투자");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 카테고리 필터링")
  void getAdvisorList_WithCategory_Success() {
    // given - 투자전략 카테고리를 찾아서 사용
    List<CategoryResponse> categories = categoryService.getAllCategories();
    Long investmentCategoryId = categories.stream()
        .filter(cat -> "투자전략".equals(cat.name()))
        .findFirst()
        .orElseThrow()
        .id();

    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, null, investmentCategoryId, null, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("김투자");
    assertThat(result.getContent().get(0).categories()).contains("투자전략");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 여러 카테고리 필터링")
  void getAdvisorList_WithMultipleCategories_Success() {
    // given - 저축계획, 투자전략 카테고리를 찾아서 사용
    List<CategoryResponse> categories = categoryService.getAllCategories();
    List<Long> categoryIds = categories.stream()
        .filter(cat -> "저축계획".equals(cat.name()) || "투자전략".equals(cat.name()))
        .map(CategoryResponse::id)
        .toList();

    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, null, null, categoryIds, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent())
        .extracting(AdvisorListResponse::name)
        .contains("김투자", "박저축");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 가격 범위 필터링")
  void getAdvisorList_WithPriceRange_Success() {
    // given
    BigDecimal minPrice = new BigDecimal("30000");
    BigDecimal maxPrice = new BigDecimal("40000");
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, null, null, null, minPrice, maxPrice, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(2); // 박저축(30000), 이부채(40000)
    assertThat(result.getContent())
        .extracting(AdvisorListResponse::name)
        .contains("박저축", "이부채");
  }

  @Test
  @DisplayName("전문가 목록 조회 - 온라인 전용")
  void getAdvisorList_OnlineOnly_Success() {
    // given
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, null, null, null, null, null, null, true);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(2); // 김투자, 이부채 (isOnline=true)
    assertThat(result.getContent())
        .extracting(AdvisorListResponse::name)
        .contains("김투자", "이부채");
    assertThat(result.getContent())
        .extracting(AdvisorListResponse::isOnline)
        .allMatch(isOnline -> isOnline);
  }

  @Test
  @DisplayName("전문가 목록 조회 - 가격 오름차순 정렬")
  void getAdvisorList_SortByPriceAsc_Success() {
    // given
    AdvisorSearchRequest request = new AdvisorSearchRequest(0, 20, "price_asc", null, null, null, null, null, null);

    // when
    Page<AdvisorListResponse> result = advisorService.getAdvisorList(request);

    // then
    assertThat(result.getContent()).hasSize(3);
    // 가격순: 박저축(30000) < 이부채(40000) < 김투자(50000)
    assertThat(result.getContent().get(0).name()).isEqualTo("박저축");
    assertThat(result.getContent().get(1).name()).isEqualTo("이부채");
    assertThat(result.getContent().get(2).name()).isEqualTo("김투자");
  }

  @Test
  @DisplayName("전문가 상세 조회 - 성공")
  void getAdvisorDetail_Success() {
    // given - 김투자 ID를 동적으로 찾기
    List<AdvisorListResponse> advisors = advisorService.getAdvisorList(
        new AdvisorSearchRequest(0, 20, null, null, null, null, null, null, null)
    ).getContent();

    Long kimInvestorId = advisors.stream()
        .filter(advisor -> "김투자".equals(advisor.name()))
        .findFirst()
        .orElseThrow()
        .id();

    // when
    AdvisorDetailResponse result = advisorService.getAdvisorDetail(kimInvestorId);

    // then
    assertThat(result.id()).isEqualTo(kimInvestorId);
    assertThat(result.name()).isEqualTo("김투자");
    assertThat(result.userId()).isEqualTo(101L);
    assertThat(result.bio()).contains("투자 전문가");
    assertThat(result.price()).isEqualTo(new BigDecimal("50000.00"));
    assertThat(result.isOnline()).isTrue();
    assertThat(result.available()).isTrue();
    assertThat(result.categories()).contains("투자전략");
  }

  @Test
  @DisplayName("전문가 상세 조회 - 추천 전문가 포함")
  void getAdvisorDetail_WithRecommendations_Success() {
    // given
    Long advisorId = 1L; // 김투자

    // when
    AdvisorDetailResponse result = advisorService.getAdvisorDetail(advisorId);

    // then
    assertThat(result.recommendedAdvisors()).isNotNull();
    // 김투자와 같은 카테고리를 가진 다른 전문가들이 추천되어야 함
  }

  @Test
  @DisplayName("전문가 상세 조회 - 존재하지 않는 전문가")
  void getAdvisorDetail_NotFound_ThrowsException() {
    // given
    Long advisorId = 999L;

    // when & then
    assertThatThrownBy(() -> advisorService.getAdvisorDetail(advisorId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 전문가입니다.");
  }

  @Test
  @DisplayName("사용자 ID로 전문가 조회 - 성공")
  void getAdvisorByUserId_Success() {
    // given
    Long userId = 101L; // 김투자

    // when
    AdvisorDetailResponse result = advisorService.getAdvisorByUserId(userId);

    // then
    assertThat(result.userId()).isEqualTo(101L);
    assertThat(result.name()).isEqualTo("김투자");
    assertThat(result.bio()).contains("투자 전문가");
  }

  @Test
  @DisplayName("사용자 ID로 전문가 조회 - 전문가가 아닌 경우")
  void getAdvisorByUserId_NotAdvisor_ThrowsException() {
    // given
    Long userId = 999L;

    // when & then
    assertThatThrownBy(() -> advisorService.getAdvisorByUserId(userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당 사용자는 전문가가 아닙니다.");
  }

  @Test
  @DisplayName("온라인 상태 업데이트 - 성공")
  void updateOnlineStatus_Success() {
    // given
    Long advisorId = 1L; // 김투자 (현재 online=true)
    Boolean newStatus = false;

    // when
    advisorService.updateOnlineStatus(advisorId, newStatus);

    // then
    Advisor updatedAdvisor = advisorRepository.findById(advisorId).orElseThrow();
    assertThat(updatedAdvisor.getIsOnline()).isFalse();
  }

  @Test
  @DisplayName("상담 가능 여부 업데이트 - 성공")
  void updateAvailability_Success() {
    // given
    Long advisorId = 1L; // 김투자 (현재 available=true)
    Boolean newAvailability = false;

    // when
    advisorService.updateAvailability(advisorId, newAvailability);

    // then
    Advisor updatedAdvisor = advisorRepository.findById(advisorId).orElseThrow();
    assertThat(updatedAdvisor.getAvailable()).isFalse();
  }

  @Test
  @DisplayName("온라인 상태 업데이트 - 존재하지 않는 전문가")
  void updateOnlineStatus_NotFound_ThrowsException() {
    // given
    Long advisorId = 999L;
    Boolean newStatus = false;

    // when & then
    assertThatThrownBy(() -> advisorService.updateOnlineStatus(advisorId, newStatus))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 전문가입니다.");
  }

  @Test
  @DisplayName("전문가 존재 여부 확인 - 존재함")
  void existsByUserId_Exists_ReturnsTrue() {
    // given - 실제 존재하는 사용자 ID 확인
    List<AdvisorListResponse> advisors = advisorService.getAdvisorList(
        new AdvisorSearchRequest(0, 20, null, null, null, null, null, null, null)
    ).getContent();

    // 김투자의 userId 확인
    AdvisorDetailResponse kimInvestor = advisors.stream()
        .filter(advisor -> "김투자".equals(advisor.name()))
        .findFirst()
        .map(advisor -> advisorService.getAdvisorDetail(advisor.id()))
        .orElseThrow(() -> new AssertionError("김투자를 찾을 수 없습니다"));

    Long userId = kimInvestor.userId();

    // when
    boolean result = advisorService.existsByUserId(userId);

    // then
    assertThat(result).isTrue();
    assertThat(userId).isEqualTo(101L); // 확인용
  }

  @Test
  @DisplayName("전문가 존재 여부 확인 - 존재하지 않음")
  void existsByUserId_NotExists_ReturnsFalse() {
    // given
    Long userId = 999L;

    // when
    boolean result = advisorService.existsByUserId(userId);

    // then
    assertThat(result).isFalse();
  }
}
