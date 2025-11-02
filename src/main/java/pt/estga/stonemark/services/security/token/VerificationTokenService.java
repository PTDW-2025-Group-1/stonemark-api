package pt.estga.stonemark.services.security.token;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Optional;

public interface VerificationTokenService {

    VerificationToken createAndSaveToken(User user, VerificationTokenPurpose purpose);

    Optional<VerificationToken> findByToken(String token);

    boolean isTokenValid(String token);

    void revokeToken(String token);

}
