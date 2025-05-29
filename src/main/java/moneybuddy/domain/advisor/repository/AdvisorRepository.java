package moneybuddy.domain.advisor.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import moneybuddy.domain.advisor.entity.Advisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

  // 활성화된 전문가들 조회
  Page<Advisor> findByAvailableTrue(Pageable pageable);

  // 온라인 상태인 전문가들 조회
  Page<Advisor> findByIsOnlineTrueAndAvailableTrue(Pageable pageable);

  // 사용자 ID로 전문가 조회
  Optional<Advisor> findByUserId(Long userId);

  // 카테고리별 전문가 조회
  @Query("SELECT a FROM Advisor a "
      + "JOIN a.advisorTags at "
      + "JOIN at.category c "
      + "WHERE c.id = :categoryId AND a.available = true")
  Page<Advisor> findByCategoryIdAndAvailableTrue(@Param("categoryId") Long categoryId, Pageable pageable);

  // 여러 카테고리에 해당하는 전문가 조회
  @Query("SELECT DISTINCT a FROM Advisor a "
      + "JOIN a.advisorTags at "
      + "JOIN at.category c "
      + "WHERE c.id IN :categoryIds AND a.available = true")
  Page<Advisor> findByCategoryIdsAndAvailableTrue(@Param("categoryIds")List<Long> categoryIds, Pageable pageable);

  // 가격 범위로 전문가 조회
  Page<Advisor> findByPriceBetweenAndAvailableTrue(
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Pageable pageable
  );

  // 전문가 이름으로 검색
  Page<Advisor> findByNameContainingIgnoreCaseAndAvailableTrue(String name, Pageable pageable);

  // 전문가 이름 또는 자기소개로 검색
  @Query("SELECT a FROM Advisor a "
      + "WHERE (LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
      + "OR LOWER(a.bio) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
      + "AND a.available = true")
  Page<Advisor> findByKeywordAndAvailableTrue(@Param("keyword") String keyword, Pageable pageable);

  // 전문가 존재 여부 확인
  boolean existsByUserId(Long userId);

  // 추천 전문가 조회 (동일 카테고리의 다른 전문가들)
  @Query("SELECT DISTINCT a FROM Advisor a "
      + "JOIN a.advisorTags at "
      + "JOIN at.category c "
      + "WHERE c.id IN ("
      + "    SELECT c2.id FROM Advisor a2 "
      + "    JOIN a2.advisorTags at2 "
      + "    JOIN at2.category c2 "
      + "    WHERE a2.id = :advisorId"
      + ") AND a.id != :advisorId AND a.available = true")
  List<Advisor> findRecommendedAdvisors(@Param("advisorId") Long advisorId, Pageable pageable);

}
