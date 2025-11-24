package pt.estga.stonemark.services.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.repositories.token.RefreshTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
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

    @Override
    public void revokeAllByUser(User user) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUser(user);
        refreshTokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(refreshTokens);
    }

    @Override
    public void revokeAll(List<RefreshToken> tokens) {
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public List<RefreshToken> findAllValidByUser(User user) {
        return refreshTokenRepository.findAllByUser(user);
    }
}
