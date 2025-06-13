package moneybuddy.domain.consultation.entity;

import jakarta.persistence.*;
import lombok.*;
import moneybuddy.domain.user.entity.User;
import moneybuddy.global.enums.ConsultationStatus;
import moneybuddy.global.enums.PaymentMethod;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 내담자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // 상담사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    private User consultant;

    // 상담 주제 또는 카테고리 (예: 불안장애, 커리어 고민)
    @Column(nullable = false)
    private String topic;

    // 상담 시간 (분 단위)
    @Column(nullable = false)
    private int durationMinutes;

    // 결제 금액 (원화 단위)
    @Column(nullable = false)
    private int amount;

    // 결제 수단 (예: 카드, 카카오페이, 토스 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // 결제 완료 시간
    @Column(nullable = false)
    private LocalDateTime paidAt;

    // 상담 상태 (예약됨, 완료됨, 취소됨 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationStatus status;

    // 상담방 연결 여부 (필요 시)
    private boolean consultationRoomCreated;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
