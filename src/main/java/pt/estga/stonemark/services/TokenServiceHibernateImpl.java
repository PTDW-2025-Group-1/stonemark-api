package pt.estga.stonemark.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.Token;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.TokenType;
import pt.estga.stonemark.exceptions.TokenNotFoundException;
import pt.estga.stonemark.repositories.TokenRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceHibernateImpl implements TokenService {

    private final TokenRepository tokenRepository;

    private static String mask(String token) {
        if (token == null) return "null";
        int len = token.length();
        if (len <= 10) return "****";
        return token.substring(0, 6) + "..." + token.substring(len - 4);
    }

    @Override
    public List<Token> findAllValidByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return tokenRepository.findAllByUserIdAndExpiredFalseAndRevokedFalse(userId);
    }

    @Override
    public Optional<Token> findByToken(String token) {
        Objects.requireNonNull(token, "token must not be null");
        return tokenRepository.findByToken(token);
    }

    @Override
    public boolean isTokenActive(String token) {
        Objects.requireNonNull(token, "refreshToken must not be null");

        var tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.debug("Token not found: {}", mask(token));
            return false;
        }
        var storedToken = tokenOpt.get();
        boolean isValid = !storedToken.isRevoked() && !storedToken.isExpired();
        log.debug("Token id={} token={} is valid={}", storedToken.getId(), mask(token), isValid);
        return isValid;
    }

    @Transactional
    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");

        var token = Token.builder()
                .user(User.builder().id(userId).build())
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .build();
        tokenRepository.save(token);
        log.debug("Saved refresh token for user id={}", userId);
    }

    @Override
    @Transactional
    public void saveAccessToken(Long userId, String jwtToken, String refreshToken) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(jwtToken, "jwtToken must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");

        revokeAllByRefreshToken(refreshToken);

        var token = Token.builder()
                .user(User.builder().id(userId).build())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
        tokenRepository.save(token);
        log.debug("Saved access token for user id={}", userId);
    }

    @Override
    @Transactional
    public void revoke(String jwtToken) {
        Objects.requireNonNull(jwtToken, "token must not be null");

        var storedToken = tokenRepository.findByToken(jwtToken)
                .orElseThrow(() -> new TokenNotFoundException(mask(jwtToken)));
        if (storedToken.isRevoked()) {
            log.debug("Token id={} token={} is already revoked", storedToken.getId(), mask(jwtToken));
            return;
        }
        storedToken.revoke();
        tokenRepository.save(storedToken);
        log.debug("Revoked token id={} token={}", storedToken.getId(), mask(jwtToken));
    }

    @Transactional
    @Override
    public void revokeAllByRefreshToken(String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");

        var tokens = tokenRepository.findAllByRefreshTokenAndRevokedFalse(refreshToken);
        if (tokens.isEmpty()) {
            log.debug("No valid tokens to revoke for refreshToken={}", mask(refreshToken));
            return;
        }
        tokens.forEach(Token::revoke);
        tokenRepository.saveAll(tokens);
        log.debug("Revoked {} tokens for refreshToken={}", tokens.size(), mask(refreshToken));
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId) {
        Objects.requireNonNull(userId, "id must not be null");
        var validUserTokens = findAllValidByUserId(userId);
        if (validUserTokens.isEmpty()) {
            log.debug("No valid tokens to revoke for user id={}", userId);
            return;
        }
        validUserTokens.forEach(Token::revoke);
        tokenRepository.saveAll(validUserTokens);
        log.debug("Revoked {} tokens for user id={}", validUserTokens.size(), userId);
    }

    @Override
    @Transactional
    public void deleteAllRevokedAndExpired() {
        tokenRepository.deleteAllByExpiredTrueOrRevokedTrue();
    }
}
