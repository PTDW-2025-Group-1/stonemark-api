package pt.estga.security.services;

import pt.estga.security.entities.AccessToken;
import pt.estga.security.entities.RefreshToken;

import java.util.Optional;

public interface AccessTokenService {

    Optional<AccessToken> findByToken(String token);

    boolean isTokenValid(String token);

    void revokeToken(String token);

    Optional<AccessToken> findByTokenWithUser(String token);

    void revokeAllByRefreshToken(RefreshToken refreshToken);

    AccessToken createToken(Long userId, String tokenValue, RefreshToken refreshToken);

    void revokeAllByUserId(Long userId);

}