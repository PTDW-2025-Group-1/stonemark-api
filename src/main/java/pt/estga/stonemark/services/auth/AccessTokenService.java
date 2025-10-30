package pt.estga.stonemark.services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.AccessToken;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.repositories.token.AccessTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AccessTokenService extends BaseTokenServiceImpl<AccessToken, AccessTokenRepository> {

    @Value("${application.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    private final UserRepository userRepository;

    public AccessTokenService(AccessTokenRepository repository, UserRepository userRepository) {
        super(repository);
        this.userRepository = userRepository;
    }

    public Optional<AccessToken> findByTokenWithUser(String token) {
        return getRepository().findByTokenWithUser(token);
    }

    public void revokeAllByRefreshToken(RefreshToken refreshToken) {
        getRepository().revokeAllByRefreshToken(refreshToken);
    }

    public AccessToken createToken(String username, String tokenValue, RefreshToken refreshToken) {
        return userRepository.findByEmail(username)
                .map(user -> {
                    AccessToken accessToken = AccessToken.builder()
                            .user(user)
                            .token(tokenValue)
                            .refreshToken(refreshToken)
                            .expiresAt(Instant.now().plus(accessTokenExpiration, ChronoUnit.MILLIS))
                            .build();
                    return getRepository().save(accessToken);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
