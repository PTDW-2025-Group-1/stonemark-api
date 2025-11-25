package pt.estga.auth.repositories;

import org.springframework.stereotype.Repository;
import pt.estga.auth.entities.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends BaseTokenRepository<VerificationToken> {
    Optional<VerificationToken> findByTokenAndRevokedFalse(String token);
    Optional<VerificationToken> findByCodeAndRevokedFalse(String code);
}
