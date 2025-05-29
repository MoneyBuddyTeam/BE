package moneybuddy.domain.advisor.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "advisors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Advisor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId; // users 테이블과 연관

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String bio; // 전문가 자기소개 (약력, 상세 소개 등)

  @Column(name = "certification_file")
  private String certificationFile; // 자격증 파일

  @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
  private Boolean available = true; // 상담 가능 여부

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price; // 상담 가격

  @Column(name = "is_online", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
  private Boolean isOnline = false; // 온라인 상태

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // 연관 관계
  @OneToMany(mappedBy = "advisor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AdvisorTag> advisorTags = new ArrayList<>();

  @Builder
  public Advisor(Long userId, String name, String bio, String certificationFile,
      Boolean available, BigDecimal price, Boolean isOnline) {
    this.userId = userId;
    this.name = name;
    this.bio = bio;
    this.certificationFile = certificationFile;
    this.available = available;
    this.price = price;
    this.isOnline = isOnline;
  }

  // 비즈니스 메서드들
  public void updateProfile(String name, String bio, BigDecimal price, Boolean available) {
    this.name = name;
    this.bio = bio;
    this.price = price;
    this.available = available;
  }

  public void updateCertificationFile(String certificationFile) {
    this.certificationFile = certificationFile;
  }

  public void updateOnlineStatus(Boolean isOnline) {
    this.isOnline = isOnline;
  }

  public void updateAvailability(Boolean available) {
    this.available = available;
  }
}
