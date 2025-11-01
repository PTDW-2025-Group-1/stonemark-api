package pt.estga.stonemark.services.token;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    @Value("${application.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    private final AccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<AccessToken> findByToken(String token) {
        return accessTokenRepository.findByToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return findByToken(token)
                .map(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public void revokeToken(String token) {
        findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            accessTokenRepository.save(t);
        });
    }

    @Override
    public Optional<AccessToken> findByTokenWithUser(String token) {
        return accessTokenRepository.findByTokenWithUser(token);
    }

    @Override
    public void revokeAllByRefreshToken(RefreshToken refreshToken) {
        accessTokenRepository.revokeAllByRefreshToken(refreshToken);
    }

    @Override
    public AccessToken createToken(String username, String tokenValue, RefreshToken refreshToken) {
        return userRepository.findByEmail(username)
                .map(user -> {
                    AccessToken accessToken = AccessToken.builder()
                            .user(user)
                            .token(tokenValue)
                            .refreshToken(refreshToken)
                            .expiresAt(Instant.now().plus(accessTokenExpiration, ChronoUnit.MILLIS))
                            .build();
                    return accessTokenRepository.save(accessToken);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
