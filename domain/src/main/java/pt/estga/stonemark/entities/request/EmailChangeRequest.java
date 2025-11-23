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
public class EmailChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String newEmail;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private VerificationToken verificationToken;

}
