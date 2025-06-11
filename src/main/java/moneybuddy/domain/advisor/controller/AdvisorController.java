package moneybuddy.domain.advisor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.advisor.dto.AdvisorDetailResponse;
import moneybuddy.domain.advisor.dto.AdvisorListResponse;
import moneybuddy.domain.advisor.dto.AdvisorSearchRequest;
import moneybuddy.domain.advisor.service.AdvisorService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisors")
@RequiredArgsConstructor
@Tag(name = "전문가 관리", description = "전문가 조회 및 관리 API")
@Slf4j
public class AdvisorController {

  private final AdvisorService advisorService;

  @Operation(
      summary = "전문가 목록 조회",
      description = "필터링, 검색, 정렬, 페이징을 지원하는 전문가 목록 조회 API"
  )
  @GetMapping
  public ResponseEntity<Page<AdvisorListResponse>> getAdvisorList(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
      @RequestParam(defaultValue = "0") Integer page,

      @Parameter(description = "페이지 크기 (1-100)", example = "20")
      @RequestParam(defaultValue = "20") Integer size,

      @Parameter(description = "정렬 기준", example = "price_asc")
      @RequestParam(defaultValue = "default") String sortBy,

      @Parameter(description = "검색 키워드 (이름, 자기소개)", example = "투자")
      @RequestParam(required = false) String keyword,

      @Parameter(description = "카테고리 ID", example = "1")
      @RequestParam(required = false) Long categoryId,

      @Parameter(description = "복수 카테고리 IDs", example = "1,2,3")
      @RequestParam(required = false) List<Long> categoryIds,

      @Parameter(description = "최소 가격", example = "30000")
      @RequestParam(required = false) BigDecimal minPrice,

      @Parameter(description = "최대 가격", example = "50000")
      @RequestParam(required = false) BigDecimal maxPrice,

      @Parameter(description = "온라인 전문가만 조회", example = "true")
      @RequestParam(required = false) Boolean onlineOnly
  ) {
    log.info(
        "전문가 목록 조회 요청 - page: {}, size: {}, sortBy: {}, keyword: {}, categoryId: {}, onlineOnly: {}",
        page, size, sortBy, keyword, categoryIds, onlineOnly);

    AdvisorSearchRequest request = new AdvisorSearchRequest(
        page, size, sortBy, keyword, categoryId, categoryIds,
        minPrice, maxPrice, onlineOnly
    );

    Page<AdvisorListResponse> advisors = advisorService.getAdvisorList(request);

    return ResponseEntity.ok(advisors);
  }

  @Operation(
      summary = "전문가 상세 조회",
      description = "전문가의 상세 정보와 추천 전문가 목록을 조회합니다."
  )
  @GetMapping("/{advisorId}")
  public ResponseEntity<AdvisorDetailResponse> getAdvisorDetail(
      @Parameter(description = "전문가 ID", example = "1")
      @PathVariable Long advisorId
  ) {
    log.info("전문가 상세 조회 요청 - advisorId: {}", advisorId);

    AdvisorDetailResponse advisor = advisorService.getAdvisorDetail(advisorId);

    return ResponseEntity.ok(advisor);
  }

  @Operation(
      summary = "사용자 ID로 전문가 조회",
      description = "사용자 ID를 통해 해당 사용자의 전문가 정보를 조회합니다."
  )
  @GetMapping("/user/{userId}")
  public ResponseEntity<AdvisorDetailResponse> getAdvisorByUserId(
      @Parameter(description = "사용자 ID", example = "101")
      @PathVariable Long userId
  ) {
    log.info("사용자 ID로 전문가 조회 요청 - userId: {}", userId);

    AdvisorDetailResponse advisor = advisorService.getAdvisorByUserId(userId);

    return ResponseEntity.ok(advisor);
  }

  @Operation(
      summary = "전문가 온라인 상태 업데이트",
      description = "전문가의 온라인 상태를 업데이트합니다."
  )
  @PutMapping("/{advisorId}/online-status")
  public ResponseEntity<Void> updateOnlineStatus(
      @Parameter(description = "전문가 ID", example = "1")
      @PathVariable Long advisorId,

      @Parameter(description = "온라인 상태", example = "true")
      @RequestParam Boolean isOnline
  ) {
    log.info("전문가 온라인 상태 업데이트 요청 - advisorId: {}, isOnline: {}", advisorId, isOnline);

    advisorService.updateOnlineStatus(advisorId, isOnline);

    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "전문가 상담 가능 여부 업데이트",
      description = "전문가의 상담 가능 여부를 업데이트합니다."
  )
  @PutMapping("/{advisorId}/availability")
  public ResponseEntity<Void> updateAvailability(
      @Parameter(description = "전문가 ID", example = "1")
      @PathVariable Long advisorId,

      @Parameter(description = "상담 가능 여부", example = "true")
      @RequestParam Boolean available
  ) {
    log.info("전문가 상담 가능 여부 업데이트 요청 - advisorId: {}, available: {}", advisorId, available);

    advisorService.updateAvailability(advisorId, available);

    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "전문가 존재 여부 확인",
      description = "사용자 ID로 전문가 등록 여부를 확인합니다."
  )
  @GetMapping("/exists/user/{userId}")
  public ResponseEntity<Boolean> checkAdvisorExists(
      @Parameter(description = "사용자 ID", example = "101")
      @PathVariable Long userId
  ) {
    log.info("전문가 존재 여부 확인 요청 - userId: {}", userId);

    boolean exists = advisorService.existsByUserId(userId);

    return ResponseEntity.ok(exists);
  }
}
