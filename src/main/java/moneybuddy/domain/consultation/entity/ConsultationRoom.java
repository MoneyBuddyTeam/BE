package moneybuddy.domain.consultation.entity;

import jakarta.persistence.*;
import lombok.*;
import moneybuddy.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultation_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 기반 상담 주문과 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_order_id", nullable = false, unique = true)
    private ConsultationOrder consultationOrder;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private boolean isClosed;

    @OneToMany(mappedBy = "consultationRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultationMessage> messages = new ArrayList<>();

    @PrePersist
    public void setDefaultLastMessageAt() {
        if (this.lastMessageAt == null) {
            this.lastMessageAt = LocalDateTime.now();
        }
    }

    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.lastMessageAt = LocalDateTime.now();
    }

    public void closeRoom() {
        this.isClosed = true;
    }

    // 유틸: 상담사/내담자 가져오기
    public Long getConsultantId() {
        return consultationOrder.getConsultant().getId();
    }

    public Long getClientId() {
        return consultationOrder.getClient().getId();
    }

    public User getConsultant() {
        return consultationOrder.getConsultant();
    }

    public User getClient() {
        return consultationOrder.getClient();
    }

}
