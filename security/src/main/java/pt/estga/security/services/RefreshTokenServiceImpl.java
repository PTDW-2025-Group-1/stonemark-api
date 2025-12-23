package pt.estga.security.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.security.entities.RefreshToken;
import pt.estga.security.repositories.RefreshTokenRepository;

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
    public RefreshToken createToken(Long userId, String tokenValue) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(Instant.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserId(userId);
        refreshTokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(refreshTokens);
    }

    @Override
    public void revokeAll(List<RefreshToken> tokens) {
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public List<RefreshToken> findAllValidByUserId(Long userId) {
        return refreshTokenRepository.findAllByUserId(userId);
    }
}
