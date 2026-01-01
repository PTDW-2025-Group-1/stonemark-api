package pt.estga.shared.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.estga.shared.enums.PrincipalType;

import jakarta.persistence.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuditActor {

    private Long id;

    @Enumerated(EnumType.STRING)
    private PrincipalType type;

    private String identifier;

}
