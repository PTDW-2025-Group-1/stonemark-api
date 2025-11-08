package pt.estga.stonemark.repositories.token;

import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.token.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends BaseTokenRepository<VerificationToken> {
    Optional<VerificationToken> findByTokenAndRevokedFalse(String token);
    Optional<VerificationToken> findByCodeAndRevokedFalse(String code);
}
