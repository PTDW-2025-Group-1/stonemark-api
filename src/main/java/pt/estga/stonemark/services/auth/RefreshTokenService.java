package pt.estga.stonemark.services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.repositories.token.RefreshTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RefreshTokenService extends BaseTokenServiceImpl<RefreshToken, RefreshTokenRepository> {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository repository, UserRepository userRepository) {
        super(repository);
        this.userRepository = userRepository;
    }

    public RefreshToken createToken(String username, String tokenValue) {
        return userRepository.findByEmail(username)
                .map(user -> {
                    RefreshToken refreshToken = RefreshToken.builder()
                            .user(user)
                            .token(tokenValue)
                            .expiresAt(Instant.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS))
                            .build();
                    return getRepository().save(refreshToken);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
