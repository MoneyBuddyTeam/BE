package moneybuddy.domain.advisor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "advisor_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdvisorTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "advisor_id", nullable = false)
  private Advisor advisor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Builder
  public AdvisorTag(Advisor advisor, Category category) {
    this.advisor = advisor;
    this.category = category;
  }

  // 연관관계 편의 메서드
  public void setAdvisor(Advisor advisor) {
    this.advisor = advisor;
    advisor.getAdvisorTags().add(this);
  }
}
