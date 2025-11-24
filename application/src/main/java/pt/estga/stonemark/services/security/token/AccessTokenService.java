package pt.estga.stonemark.services.security.token;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.AccessToken;
import pt.estga.stonemark.entities.token.RefreshToken;

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