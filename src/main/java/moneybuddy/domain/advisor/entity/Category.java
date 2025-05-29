package moneybuddy.domain.advisor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CategoryType type;

  @Builder
  public Category(String name, CategoryType type) {
    this.name = name;
    this.type = type;
  }

  // 카테고리 타입 enum
  public enum CategoryType {
    SPENDING,   // 소비
    SAVINGS,    // 저축
    INVESTMENT, // 투자
    DEBT,       // 부채
    ETC         // 기타
  }
}
