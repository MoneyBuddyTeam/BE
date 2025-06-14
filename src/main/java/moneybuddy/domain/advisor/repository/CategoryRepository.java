package moneybuddy.domain.advisor.repository;

import java.util.List;
import java.util.Optional;
import moneybuddy.domain.advisor.entity.Category;
import moneybuddy.domain.advisor.entity.Category.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 카테고리 타입별 조회
  List<Category> findByType(CategoryType type);

  // 카테고리 이름으로 조회
  Optional<Category> findByName(String name);

  // 카테고리 이름 존재 여부 확인
  boolean existsByName(String name);

  // 모든 카테고리를 이름 순으로 조회
  List<Category> findAllByOrderByNameAsc();
}
