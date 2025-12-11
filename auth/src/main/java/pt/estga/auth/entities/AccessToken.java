package pt.estga.auth.entities;

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
public class AccessToken extends BaseToken {

    @ManyToOne(fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

}
