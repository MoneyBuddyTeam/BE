package moneybuddy.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String phone;
    private String nickname;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private LoginMethod loginMethod;

    private LocalDateTime lastLoginAt;
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
