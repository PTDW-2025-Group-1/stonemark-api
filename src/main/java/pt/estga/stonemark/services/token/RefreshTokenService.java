package pt.estga.stonemark.services.token;

import pt.estga.stonemark.entities.token.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    boolean isTokenValid(String token);

    void revokeToken(String token);

    RefreshToken createToken(String username, String tokenValue);

}