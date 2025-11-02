package pt.estga.stonemark.services.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.repositories.token.RefreshTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
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
            refreshTokenRepository.save(t);
        });
    }

    @Override
    public RefreshToken createToken(String username, String tokenValue) {
        return userRepository.findByEmail(username)
                .map(user -> {
                    RefreshToken refreshToken = RefreshToken.builder()
                            .user(user)
                            .token(tokenValue)
                            .expiresAt(Instant.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS))
                            .build();
                    return refreshTokenRepository.save(refreshToken);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
