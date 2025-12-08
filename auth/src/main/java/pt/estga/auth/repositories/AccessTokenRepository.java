package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.estga.auth.entities.token.AccessToken;
import pt.estga.auth.entities.token.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends BaseTokenRepository<AccessToken> {

    @Query("SELECT t FROM AccessToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<AccessToken> findByTokenWithUser(String token);

    @Query("SELECT t FROM AccessToken t JOIN FETCH t.refreshToken WHERE t.token = :token")
    Optional<AccessToken> findByTokenWithRefreshToken(String token);

    @Modifying
    @Query("UPDATE AccessToken t SET t.isRevoked = true WHERE t.refreshToken = :refreshToken")
    void revokeAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByUserIdAndRevokedFalse(Long userId);

    void deleteByRevokedTrueAndExpiresAtBefore(Instant instant);

    List<AccessToken> findAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByRefreshTokenAndRevokedFalse(RefreshToken refreshToken);

}
