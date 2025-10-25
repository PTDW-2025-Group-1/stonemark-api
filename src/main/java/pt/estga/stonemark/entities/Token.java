package pt.estga.stonemark.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pt.estga.stonemark.enums.TokenType;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Token {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String token;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.ACCESS;

    private String refreshToken;

    @CreationTimestamp
    private Instant createdAt;

    private boolean revoked = false;

    private boolean expired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void revoke() {
        this.revoked = true;
        this.expired = true;
    }
}
