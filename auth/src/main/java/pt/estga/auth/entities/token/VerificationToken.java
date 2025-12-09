package pt.estga.auth.entities.token;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.auth.enums.VerificationPurpose;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class VerificationToken extends BaseToken {

    @Enumerated(EnumType.STRING)
    private VerificationPurpose purpose;

    @Column(unique = true)
    private String code;

    private Instant usedAt;

    /**
     * Calculates the remaining validity time of the token in milliseconds.
     *
     * @return The remaining validity time in milliseconds, or 0 if already expired.
     */
    public long getRemainingValidityMillis() {
        long expiresMillis = this.getExpiresAt().toEpochMilli();
        long currentMillis = System.currentTimeMillis();
        return Math.max(0, expiresMillis - currentMillis);
    }
}
