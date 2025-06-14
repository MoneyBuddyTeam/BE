package moneybuddy.domain.advisor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import moneybuddy.config.TestConfig;
import moneybuddy.domain.advisor.dto.CategoryResponse;
import moneybuddy.domain.advisor.entity.Category;
import moneybuddy.domain.advisor.entity.Category.CategoryType;
import moneybuddy.domain.advisor.service.CategoryService;
import moneybuddy.config.SwaggerConfig;
import moneybuddy.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    CategoryController.class,
    SwaggerConfig.class,
    GlobalExceptionHandler.class
})
@WithMockUser
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CategoryService categoryService;

  @Test
  @DisplayName("전체 카테고리 조회 - 성공")
  void getAllCategories_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(1L, "소비관리", "SPENDING", "소비"),
        new CategoryResponse(2L, "저축계획", "SAVINGS", "저축"),
        new CategoryResponse(3L, "투자전략", "INVESTMENT", "투자"),
        new CategoryResponse(4L, "부채관리", "DEBT", "부채"),
        new CategoryResponse(5L, "기타상담", "ETC", "기타")
    );

    given(categoryService.getAllCategories()).willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(5))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("소비관리"))
        .andExpect(jsonPath("$[0].type").value("SPENDING"))
        .andExpect(jsonPath("$[0].typeDisplayName").value("소비"));
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 투자")
  void getCategoriesByType_Investment_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(3L, "투자전략", "INVESTMENT", "투자")
    );

    given(categoryService.getCategoriesByType(Category.CategoryType.INVESTMENT))
        .willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories/type/INVESTMENT")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("투자전략"))
        .andExpect(jsonPath("$[0].type").value("INVESTMENT"));
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 저축")
  void getCategoriesByType_Savings_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(2L, "저축계획", "SAVINGS", "저축")
    );

    given(categoryService.getCategoriesByType(CategoryType.SAVINGS))
        .willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories/type/SAVINGS")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("저축계획"))
        .andExpect(jsonPath("$[0].typeDisplayName").value("저축"));
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 소비")
  void getCategoriesByType_Spending_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(1L, "소비관리", "SPENDING", "소비")
    );

    given(categoryService.getCategoriesByType(CategoryType.SPENDING))
        .willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories/type/SPENDING")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("소비관리"))
        .andExpect(jsonPath("$[0].typeDisplayName").value("소비"));
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 부채")
  void getCategoriesByType_Debt_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(4L, "부채관리", "DEBT", "부채")
    );

    given(categoryService.getCategoriesByType(CategoryType.DEBT))
        .willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories/type/DEBT")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("부채관리"))
        .andExpect(jsonPath("$[0].typeDisplayName").value("부채"));
  }

  @Test
  @DisplayName("카테고리 타입별 조회 - 기타")
  void getCategoriesByType_Etc_Success() throws Exception {
    // given
    List<CategoryResponse> categories = List.of(
        new CategoryResponse(5L, "기타상담", "ETC", "기타")
    );

    given(categoryService.getCategoriesByType(CategoryType.ETC))
        .willReturn(categories);

    // when & then
    mockMvc.perform(get("/api/v1/categories/type/ETC")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].typeDisplayName").value("기타"));
  }

  @Test
  @DisplayName("카테고리 상세 조회 - 성공")
  void getCategoryById_Success() throws Exception {
    // given
    CategoryResponse category = new CategoryResponse(3L, "투자전략", "INVESTMENT", "투자");
    given(categoryService.getCategoryById(3L)).willReturn(category);

    // when & then
    mockMvc.perform(get("/api/v1/categories/3")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.name").value("투자전략"))
        .andExpect(jsonPath("$.type").value("INVESTMENT"))
        .andExpect(jsonPath("$.typeDisplayName").value("투자"));
  }

  @Test
  @DisplayName("카테고리 상세 조회 - 존재하지 않는 카테고리")
  void getCategoryById_NotFound() throws Exception {
    // given
    given(categoryService.getCategoryById(999L))
        .willThrow(new IllegalArgumentException("존재하지 않는 카테고리입니다."));

    // when & then
    mockMvc.perform(get("/api/v1/categories/999")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string("존재하지 않는 카테고리입니다."));
  }

  @Test
  @DisplayName("잘못된 카테고리 타입 - 에러")
  void getCategoriesByType_InvalidType() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/categories/type/INVALID_TYPE")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("잘못된 카테고리 ID 타입 - 에러")
  void getCategoryById_InvalidIdType() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/categories/invalid")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("빈 카테고리 목록 조회")
  void getAllCategories_Empty() throws Exception {
    // given
    given(categoryService.getAllCategories()).willReturn(List.of());

    // when & then
    mockMvc.perform(get("/api/v1/categories")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
