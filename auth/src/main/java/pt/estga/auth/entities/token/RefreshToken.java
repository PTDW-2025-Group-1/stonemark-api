package pt.estga.auth.entities.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RefreshToken extends BaseToken {

    @Column(length = 64)
    private String deviceId;

    @Column(length = 45)
    private String ipAddress;

    private String userAgent;

}
