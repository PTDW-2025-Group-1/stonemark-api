package pt.estga.stonemark.services;

import jakarta.transaction.Transactional;
import pt.estga.stonemark.entities.Token;

import java.util.List;
import java.util.Optional;

public interface TokenService {

    List<Token> findAllValidByUserId(Long userId);

    Optional<Token> findByToken(String token);

    boolean isTokenActive(String refreshToken);

    @Transactional
    void saveAccessToken(Long userId, String token, String refreshToken);

    @Transactional
    void saveRefreshToken(Long userId, String refreshToken);

    @Transactional
    void revoke(String token);

    @Transactional
    void revokeAllByRefreshToken(String parentToken);

    @Transactional
    void revokeAllByUserId(Long userId);

    @Transactional
    void deleteAllRevokedAndExpired();
}
