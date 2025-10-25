package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.Token;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findAllByUserIdAndExpiredFalseAndRevokedFalse(Long userId);

    Optional<Token> findByToken(String token);

    void deleteAllByExpiredTrueOrRevokedTrue();

    List<Token> findAllByRefreshTokenAndRevokedFalse(String parentToken);

    List<Token> findByRefreshToken(String refreshToken);

}
