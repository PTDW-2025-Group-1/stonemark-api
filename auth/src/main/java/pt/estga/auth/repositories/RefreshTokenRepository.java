package pt.estga.auth.repositories;

import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.token.RefreshToken;

@Repository
public interface RefreshTokenRepository extends BaseTokenRepository<RefreshToken> {
}
