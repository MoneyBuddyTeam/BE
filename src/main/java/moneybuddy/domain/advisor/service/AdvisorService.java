package moneybuddy.domain.advisor.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.advisor.dto.AdvisorDetailResponse;
import moneybuddy.domain.advisor.dto.AdvisorListResponse;
import moneybuddy.domain.advisor.dto.AdvisorSearchRequest;
import moneybuddy.domain.advisor.entity.Advisor;
import moneybuddy.domain.advisor.repository.AdvisorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdvisorService {

  private final AdvisorRepository advisorRepository;

  /**
   * 전문가 목록 조회 (필터링 및 정렬 지원)
   */
  public Page<AdvisorListResponse> getAdvisorList(AdvisorSearchRequest request) {
    log.debug("전문가 목록 조회 요청: {}", request);

    Pageable pageable = createPageable(request);
    Page<Advisor> advisorPage;

    // 검색 조건에 따른 쿼리 실행
    if (request.keyword() != null && !request.keyword().trim().isEmpty()) {
      advisorPage = advisorRepository.findByKeywordAndAvailableTrue(request.keyword(), pageable);
    } else if (request.categoryId() != null) {
      advisorPage = advisorRepository.findByCategoryIdAndAvailableTrue(request.categoryId(),
          pageable);
    } else if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
      advisorPage = advisorRepository.findByCategoryIdsAndAvailableTrue(request.categoryIds(),
          pageable);
    } else if (request.minPrice() != null && request.maxPrice() != null) {
      advisorPage = advisorRepository.findByPriceBetweenAndAvailableTrue(request.minPrice(),
          request.maxPrice(), pageable);
    } else if (request.onlineOnly() != null && request.onlineOnly()) {
      advisorPage = advisorRepository.findByIsOnlineTrueAndAvailableTrue(pageable);
    } else {
      advisorPage = advisorRepository.findByAvailableTrue(pageable);
    }

    return advisorPage.map(AdvisorListResponse::from);
  }

  /**
   * 전문가 상세 정보 조회
   */
  @Transactional
  public AdvisorDetailResponse getAdvisorDetail(Long advisorId) {
    log.debug("전문가 상세 정보 조회: advisorId={}", advisorId);

    Advisor advisor = advisorRepository.findById(advisorId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전문가입니다."));

    // 추천 전문가 조회 (최대 4명)
    Pageable recommendPageable = PageRequest.of(0, 4);
    List<Advisor> recommendedAdvisors = advisorRepository
        .findRecommendedAdvisors(advisorId, recommendPageable);

    return AdvisorDetailResponse.from(advisor, recommendedAdvisors);
  }

  /**
   * 사용자 ID로 전문가 정보 조회
   */
  public AdvisorDetailResponse getAdvisorByUserId(Long userId) {
    log.debug("사용자 ID로 전문가 조회: userId={}", userId);

    Advisor advisor = advisorRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 전문가가 아닙니다."));

    return AdvisorDetailResponse.from(advisor, List.of());
  }

  /**
   * 전문가 온라인 상태 업데이트
   */
  @Transactional
  public void updateOnlineStatus(Long advisorId, Boolean isOnline) {
    log.debug("전문가 온라인 상태 업데이트: advisorId={}, isOnline={}", advisorId, isOnline);

    Advisor advisor = advisorRepository.findById(advisorId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전문가입니다."));

    advisor.updateOnlineStatus(isOnline);
  }

  /**
   * 전문가 상담 가능 여부 업데이트
   */
  @Transactional
  public void updateAvailability(Long advisorId, Boolean isAvailable) {
    log.debug("전문가 상담 가능 여부 업데이트: advisorId={}, isAvailable={}", advisorId, isAvailable);

    Advisor advisor = advisorRepository.findById(advisorId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전문가입니다."));

    advisor.updateAvailability(isAvailable);
  }

  /**
   * 정렬 조건에 따른 Pageable 생성
   */
  private Pageable createPageable(AdvisorSearchRequest request) {
    Sort sort = switch (request.sortBy()) {
      case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
      case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
      case "name" -> Sort.by(Sort.Direction.ASC, "name");
      case "created_at" -> Sort.by(Sort.Direction.DESC, "created_at");
      default -> Sort.by(Sort.Direction.DESC, "id");
    };

    return PageRequest.of(request.page(), request.size(), sort);
  }

  /**
   * 전문가 존재 여부 확인
   */
  public boolean existsByUserId(Long userId) {
    return advisorRepository.existsByUserId(userId);
  }
}
