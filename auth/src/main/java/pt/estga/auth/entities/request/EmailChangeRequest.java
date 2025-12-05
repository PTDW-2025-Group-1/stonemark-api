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
public class EmailChangeRequest {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String newEmail;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(nullable = false)
    private VerificationToken verificationToken;

}
