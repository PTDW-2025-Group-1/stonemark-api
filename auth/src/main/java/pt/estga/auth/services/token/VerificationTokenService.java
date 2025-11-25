package pt.estga.auth.services.token;

import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface VerificationTokenService {

    VerificationToken createAndSaveToken(User user, VerificationTokenPurpose purpose);

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByCode(String code);

    boolean isTokenValid(String token);

    void revokeToken(VerificationToken token);

}
