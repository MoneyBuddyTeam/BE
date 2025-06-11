package moneybuddy.domain.advisor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.dto.AdvisorDetailResponse;
import moneybuddy.domain.advisor.dto.AdvisorListResponse;
import moneybuddy.domain.advisor.dto.RecommendedAdvisorResponse;
import moneybuddy.domain.advisor.service.AdvisorService;
import moneybuddy.config.SwaggerConfig;
import moneybuddy.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvisorController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    AdvisorController.class,
    SwaggerConfig.class,
    GlobalExceptionHandler.class,})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@WithMockUser
@DisplayName("AdvisorController 테스트")
class AdvisorControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AdvisorService advisorService;

  @Test
  @DisplayName("전문가 목록 조회 - 성공")
  void getAdvisorList_Success() throws Exception {
    // given
    AdvisorListResponse advisorResponse = new AdvisorListResponse(
        1L,
        "김투자",
        "10년 경력의 투자 전문가입니다.",
        new BigDecimal("50000"),
        true,
        true,
        List.of("투자전략")
    );

    Page<AdvisorListResponse> advisorPage = new PageImpl<>(List.of(advisorResponse));
    given(advisorService.getAdvisorList(any())).willReturn(advisorPage);

    // when & then
    mockMvc.perform(get("/api/v1/advisors")
            .param("page", "0")
            .param("size", "20")
            .param("sortBy", "default")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("김투자"))
        .andExpect(jsonPath("$.content[0].price").value(50000));
  }

  @Test
  @DisplayName("전문가 목록 조회 - 키워드 검색")
  void getAdvisorList_WithKeyword_Success() throws Exception {
    // given
    AdvisorListResponse advisorResponse = new AdvisorListResponse(
        1L,
        "김투자",
        "투자 전문가입니다.",
        new BigDecimal("50000"),
        true,
        true,
        List.of("투자전략")
    );

    Page<AdvisorListResponse> advisorPage = new PageImpl<>(List.of(advisorResponse));
    given(advisorService.getAdvisorList(any())).willReturn(advisorPage);

    // when & then
    mockMvc.perform(get("/api/v1/advisors")
            .param("keyword", "투자")
            .param("page", "0")
            .param("size", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("김투자"));
  }

  @Test
  @DisplayName("전문가 목록 조회 - 카테고리 필터링")
  void getAdvisorList_WithCategory_Success() throws Exception {
    // given
    AdvisorListResponse advisorResponse = new AdvisorListResponse(
        1L,
        "김투자",
        "투자 전문가입니다.",
        new BigDecimal("50000"),
        true,
        true,
        List.of("투자전략")
    );

    Page<AdvisorListResponse> advisorPage = new PageImpl<>(List.of(advisorResponse));
    given(advisorService.getAdvisorList(any())).willReturn(advisorPage);

    // when & then
    mockMvc.perform(get("/api/v1/advisors")
            .param("categoryId", "1")
            .param("page", "0")
            .param("size", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].categories[0]").value("투자전략"));
  }

  @Test
  @DisplayName("전문가 목록 조회 - 가격 범위 필터링")
  void getAdvisorList_WithPriceRange_Success() throws Exception {
    // given
    AdvisorListResponse advisorResponse = new AdvisorListResponse(
        1L,
        "김투자",
        "투자 전문가입니다.",
        new BigDecimal("50000"),
        true,
        true,
        List.of("투자전략")
    );

    Page<AdvisorListResponse> advisorPage = new PageImpl<>(List.of(advisorResponse));
    given(advisorService.getAdvisorList(any())).willReturn(advisorPage);

    // when & then
    mockMvc.perform(get("/api/v1/advisors")
            .param("minPrice", "30000")
            .param("maxPrice", "60000")
            .param("page", "0")
            .param("size", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].price").value(50000));
  }

  @Test
  @DisplayName("전문가 상세 조회 - 성공")
  void getAdvisorDetail_Success() throws Exception {
    // given
    RecommendedAdvisorResponse recommendedAdvisor =
        new RecommendedAdvisorResponse(
            2L, "박저축", "저축 전문가", new BigDecimal("30000"), false
        );

    AdvisorDetailResponse advisorResponse = new AdvisorDetailResponse(
        1L,
        101L,
        "김투자",
        "10년 경력의 투자 전문가입니다. 주식, 펀드, 부동산 투자에 대한 깊은 지식을 보유하고 있습니다.",
        "cert1.pdf",
        new BigDecimal("50000"),
        true,
        true,
        LocalDateTime.now(),
        List.of("투자전략", "기타상담"),
        List.of(recommendedAdvisor)
    );

    given(advisorService.getAdvisorDetail(1L)).willReturn(advisorResponse);

    // when & then
    mockMvc.perform(get("/api/v1/advisors/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("김투자"))
        .andExpect(jsonPath("$.userId").value(101))
        .andExpect(jsonPath("$.bio").exists())
        .andExpect(jsonPath("$.price").value(50000))
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.recommendedAdvisors").isArray())
        .andExpect(jsonPath("$.recommendedAdvisors[0].name").value("박저축"));
  }

  @Test
  @DisplayName("전문가 상세 조회 - 존재하지 않는 전문가")
  void getAdvisorDetail_NotFound() throws Exception {
    // given
    given(advisorService.getAdvisorDetail(999L))
        .willThrow(new IllegalArgumentException("존재하지 않는 전문가입니다."));

    // when & then
    mockMvc.perform(get("/api/v1/advisors/999")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string("존재하지 않는 전문가입니다."));
  }

  @Test
  @DisplayName("사용자 ID로 전문가 조회 - 성공")
  void getAdvisorByUserId_Success() throws Exception {
    // given
    AdvisorDetailResponse advisorResponse = new AdvisorDetailResponse(
        1L,
        101L,
        "김투자",
        "투자 전문가입니다.",
        "cert1.pdf",
        new BigDecimal("50000"),
        true,
        true,
        LocalDateTime.now(),
        List.of("투자전략"),
        List.of()
    );

    given(advisorService.getAdvisorByUserId(101L)).willReturn(advisorResponse);

    // when & then
    mockMvc.perform(get("/api/v1/advisors/user/101")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(101))
        .andExpect(jsonPath("$.name").value("김투자"));
  }

  @Test
  @DisplayName("온라인 상태 업데이트 - 성공")
  void updateOnlineStatus_Success() throws Exception {
    // when & then
    mockMvc.perform(put("/api/v1/advisors/1/online-status")
            .param("isOnline", "false")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("상담 가능 여부 업데이트 - 성공")
  void updateAvailability_Success() throws Exception {
    // when & then
    mockMvc.perform(put("/api/v1/advisors/1/availability")
            .param("available", "false")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("전문가 존재 여부 확인 - 존재함")
  void checkAdvisorExists_True() throws Exception {
    // given
    given(advisorService.existsByUserId(101L)).willReturn(true);

    // when & then
    mockMvc.perform(get("/api/v1/advisors/exists/user/101")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(true));
  }

  @Test
  @DisplayName("전문가 존재 여부 확인 - 존재하지 않음")
  void checkAdvisorExists_False() throws Exception {
    // given
    given(advisorService.existsByUserId(999L)).willReturn(false);

    // when & then
    mockMvc.perform(get("/api/v1/advisors/exists/user/999")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(false));
  }

  @Test
  @DisplayName("잘못된 파라미터 타입 - 에러")
  void getAdvisorDetail_InvalidParameter() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/advisors/invalid")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("필수 파라미터 누락 - 에러")
  void updateOnlineStatus_MissingParameter() throws Exception {
    // when & then
    mockMvc.perform(put("/api/v1/advisors/1/online-status")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}
