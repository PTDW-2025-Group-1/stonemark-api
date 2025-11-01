package pt.estga.stonemark.entities.request;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmailChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String newEmail;

        @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private VerificationToken verificationToken;

}
