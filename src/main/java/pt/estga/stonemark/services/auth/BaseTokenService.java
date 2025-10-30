package pt.estga.stonemark.services.auth;

import pt.estga.stonemark.entities.token.BaseToken;

import java.util.Optional;

public interface BaseTokenService<T extends BaseToken> {

    Optional<T> findByToken(String token);

    boolean isTokenValid(String token);

    void revokeToken(String token);
}
