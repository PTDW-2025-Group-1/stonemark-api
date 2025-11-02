package pt.estga.stonemark.entities.request;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PasswordResetRequest {

    @Id
    @GeneratedValue
    private Long id;

    private String newPassword;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private VerificationToken verificationToken;

}
