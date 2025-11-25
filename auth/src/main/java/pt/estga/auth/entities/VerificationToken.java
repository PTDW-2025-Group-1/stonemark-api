package pt.estga.auth.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class VerificationToken extends BaseToken {

    @Enumerated(EnumType.STRING)
    private VerificationTokenPurpose purpose;

    @Column(unique = true)
    private String code;

    private Instant usedAt;

}
