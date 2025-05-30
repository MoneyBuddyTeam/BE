package moneybuddy.domain.user.entity;

import moneybuddy.global.enums.PrivacyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Boolean notificationEnabled;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel privacyLevel;
}
