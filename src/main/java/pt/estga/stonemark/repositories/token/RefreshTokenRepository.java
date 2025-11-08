package pt.estga.stonemark.repositories.token;

import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.token.RefreshToken;

@Repository
public interface RefreshTokenRepository extends BaseTokenRepository<RefreshToken> {
}
