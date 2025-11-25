package pt.estga.auth.services.token;

import pt.estga.auth.entities.token.AccessToken;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface AccessTokenService {

    Optional<AccessToken> findByToken(String token);

    boolean isTokenValid(String token);

    void revokeToken(String token);

    Optional<AccessToken> findByTokenWithUser(String token);

    void revokeAllByRefreshToken(RefreshToken refreshToken);

    AccessToken createToken(String username, String tokenValue, RefreshToken refreshToken);

    void revokeAllByUser(User user);

}