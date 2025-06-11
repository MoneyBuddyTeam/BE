package moneybuddy.domain.advisor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.advisor.dto.CategoryResponse;
import moneybuddy.domain.advisor.entity.Category;
import moneybuddy.domain.advisor.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리 관리", description = "상담 카테고리 조회 API")
@Slf4j
public class CategoryController {

  private final CategoryService categoryService;

  @Operation(
      summary = "전체 카테고리 조회",
      description = "시스템에 등록된 모든 카테고리를 이름순으로 조회합니다."
  )
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    log.info("전체 카테고리 조회 요청");

    List<CategoryResponse> categories = categoryService.getAllCategories();

    return ResponseEntity.ok(categories);
  }

  @Operation(
      summary = "카테고리 타입별 조회",
      description = "특정 카테고리 타입에 해당하는 카테고리들을 조회합니다."
  )
  @GetMapping("/type/{type}")
  public ResponseEntity<List<CategoryResponse>> getCategoriesByType(
      @Parameter(
          description = "카테고리 타입",
          example = "INVESTMENT"
      )
      @PathVariable Category.CategoryType type
  ) {
    log.info("카테고리 타입별 조회 요청 - type: {}", type);

    List<CategoryResponse> categories = categoryService.getCategoriesByType(type);

    return ResponseEntity.ok(categories);
  }

  @Operation(
      summary = "카테고리 상세 조회",
      description = "특정 카테고리의 상세 정보를 조회합니다."
  )
  @GetMapping("/{categoryId}")
  public ResponseEntity<CategoryResponse> getCategoryById(
      @Parameter(description = "카테고리 ID", example = "1")
      @PathVariable Long categoryId
  ) {
    log.info("카테고리 상세 조회 요청 - categoryId: {}", categoryId);

    CategoryResponse category = categoryService.getCategoryById(categoryId);

    return ResponseEntity.ok(category);
  }

}
