package moneybuddy.domain.advisor.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.advisor.dto.CategoryResponse;
import moneybuddy.domain.advisor.entity.Category;
import moneybuddy.domain.advisor.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /**
   * 모든 카테고리 조회
   */
  public List<CategoryResponse> getAllCategories() {
    log.debug("모든 카테고리 조회");

    List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
    return categories.stream()
        .map(CategoryResponse::from)
        .toList();
  }

  /**
   * 카테고리 타입별 조회
   */
  public List<CategoryResponse> getCategoriesByType(Category.CategoryType type) {
    log.debug("카테고리 타입별 조회: type={}", type);

    List<Category> categories = categoryRepository.findByType(type);
    return categories.stream()
        .map(CategoryResponse::from)
        .toList();
  }

  /**
   * 카테고리 ID로 조회
   */
  public CategoryResponse getCategoryById(Long categoryId) {
    log.debug("카테고리 ID로 조회: categoryId={}", categoryId);

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

    return CategoryResponse.from(category);
  }
}
