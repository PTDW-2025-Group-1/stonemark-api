package pt.estga.auth.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import pt.estga.auth.enums.ActionCodeType;
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

    private User user;

    private boolean consumed;

}
