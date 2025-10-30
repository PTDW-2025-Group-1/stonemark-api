package pt.estga.stonemark.repositories.token;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.token.AccessToken;
import pt.estga.stonemark.entities.token.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends BaseTokenRepository<AccessToken> {

    @Query("SELECT t FROM AccessToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<AccessToken> findByTokenWithUser(String token);

    @Modifying
    @Query("UPDATE AccessToken t SET t.revoked = true WHERE t.refreshToken = :refreshToken")
    void revokeAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByUserIdAndRevokedFalse(Long userId);

    void deleteByRevokedTrueAndExpiresAtBefore(Instant instant);

    List<AccessToken> findAllByRefreshToken(RefreshToken refreshToken);

    List<AccessToken> findAllByRefreshTokenAndRevokedFalse(RefreshToken refreshToken);

}
