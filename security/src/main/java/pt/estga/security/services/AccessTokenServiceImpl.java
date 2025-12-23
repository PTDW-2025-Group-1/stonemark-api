package pt.estga.security.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.security.entities.AccessToken;
import pt.estga.security.entities.RefreshToken;
import pt.estga.security.repositories.AccessTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    @Value("${application.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    private final AccessTokenRepository accessTokenRepository;

    @Override
    public Optional<AccessToken> findByToken(String token) {
        return accessTokenRepository.findByTokenWithRefreshToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return findByToken(token)
                .map(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    @Transactional
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
    public AccessToken createToken(Long userId, String tokenValue, RefreshToken refreshToken) {
        AccessToken accessToken = AccessToken.builder()
                .userId(userId)
                .token(tokenValue)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plus(accessTokenExpiration, ChronoUnit.MILLIS))
                .build();
        return accessTokenRepository.save(accessToken);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        List<AccessToken> accessTokens = accessTokenRepository.findAllByUserId(userId);
        if (!accessTokens.isEmpty()) {
            accessTokens.forEach(t -> t.setRevoked(true));
            accessTokenRepository.saveAll(accessTokens);
        }
    }
}
