package pt.estga.stonemark.entities.token;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(indexes = {
        @Index(columnList = "token"),
        @Index(columnList = "user_id"),
        @Index(columnList = "refresh_token_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AccessToken extends BaseToken {

    @ManyToOne(fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

}
