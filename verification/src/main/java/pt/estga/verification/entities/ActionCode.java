package pt.estga.verification.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ActionCode {

    @Id
    @GeneratedValue
    private String id;

    private String code;

    private Instant expiresAt;

    private ActionCodeType type;

    @ManyToOne
    private User user;

    private boolean consumed;

}
