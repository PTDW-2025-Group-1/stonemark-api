package pt.estga.auth.entities.request;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.user.entities.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PhoneChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String newPhone;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private VerificationToken verificationToken;

}
