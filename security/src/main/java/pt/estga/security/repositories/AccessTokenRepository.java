package pt.estga.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.estga.security.entities.AccessToken;
import pt.estga.security.entities.RefreshToken;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    Optional<AccessToken> findByToken(String token);

    @Query("SELECT t FROM AccessToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<AccessToken> findByTokenWithUser(String token);

    @Query("SELECT t FROM AccessToken t JOIN FETCH t.refreshToken WHERE t.token = :token")
    Optional<AccessToken> findByTokenWithRefreshToken(String token);

    @Modifying
    @Query("UPDATE AccessToken t SET t.isRevoked = true WHERE t.refreshToken = :refreshToken")
    void revokeAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByUser(User user);

    List<AccessToken> findAllByUserIdAndIsRevokedFalse(Long userId);

    void deleteByIsRevokedTrueAndExpiresAtBefore(Instant instant);

    List<AccessToken> findAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByRefreshTokenAndIsRevokedFalse(RefreshToken refreshToken);

}
