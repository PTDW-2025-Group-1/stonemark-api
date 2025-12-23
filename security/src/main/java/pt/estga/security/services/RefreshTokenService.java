package pt.estga.security.services;

import pt.estga.security.entities.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    void revokeToken(RefreshToken token);

    RefreshToken createToken(Long userId, String tokenValue);

    List<RefreshToken> findAllValidByUserId(Long user);

    void revokeAllByUserId(Long user);

    void revokeAll(List<RefreshToken> tokens);

}