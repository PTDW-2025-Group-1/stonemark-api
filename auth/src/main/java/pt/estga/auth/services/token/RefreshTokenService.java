package pt.estga.auth.services.token;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    void revokeToken(RefreshToken token);

    RefreshToken createToken(String username, String tokenValue);

    List<RefreshToken> findAllValidByUser(User user);

    void revokeAllByUser(User user);

    void revokeAll(List<RefreshToken> tokens);

}