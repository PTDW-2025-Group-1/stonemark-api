package pt.estga.security.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.security.enums.TokenType;

import java.time.Instant;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class BaseToken {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(nullable = false, unique = true)
    private String token;

    private Long userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder.Default
    private boolean isRevoked = false;

}
