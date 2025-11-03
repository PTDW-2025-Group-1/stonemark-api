package pt.estga.stonemark.entities.token;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VerificationToken extends BaseToken {

    @Enumerated(EnumType.STRING)
    private VerificationTokenPurpose purpose;

    @Column(unique = true)
    private String code;

    private Instant usedAt;

}
