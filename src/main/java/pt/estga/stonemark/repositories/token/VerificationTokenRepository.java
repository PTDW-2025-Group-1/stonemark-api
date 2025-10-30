package pt.estga.stonemark.repositories.token;

import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.token.VerificationToken;

@Repository
public interface VerificationTokenRepository extends BaseTokenRepository<VerificationToken> {
}
