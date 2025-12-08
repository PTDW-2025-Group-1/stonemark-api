package pt.estga.auth.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TwoFactorCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenPurpose purpose; // e.g., SMS_2FA, EMAIL_2FA

    @Column(nullable = false)
    private Instant expiryDate;
}
