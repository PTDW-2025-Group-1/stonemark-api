import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.estga.security.services.JwtService;
import pt.estga.security.services.JwtServiceImpl;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final Long userId = 123L;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long accessTokenExpiration = 1000 * 60 * 24; // 24 hours
    private final long refreshTokenExpiration = 1000 * 60 * 24 * 7; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(secretKey, accessTokenExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) jwtService).init(); // Manually call PostConstruct
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateAccessToken(userId);
        Long extractedUserId = jwtService.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUserId_FromToken_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.string";
        Long extractedUserId = jwtService.getUserIdFromToken(invalidToken);
        assertNull(extractedUserId);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken(userId);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userId));
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(userId);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userId));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(userId);
        assertTrue(jwtService.isTokenValid(token, userId));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtService.isTokenValid(invalidToken, userId));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        long shortExpiration = 10; // 10 milliseconds
        JwtService shortLivedJwtService = new JwtServiceImpl(secretKey, shortExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) shortLivedJwtService).init(); // Manually call PostConstruct
        String token = shortLivedJwtService.generateAccessToken(userId);

        Thread.sleep(50); // Wait for the token to expire

        assertFalse(shortLivedJwtService.isTokenValid(token, userId));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateAccessToken(userId);
        Long otherUserId = 456L;
        assertFalse(jwtService.isTokenValid(token, otherUserId));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTokenSignedWithDifferentKey() {
        String otherSecretKey = "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";
        JwtService otherJwtService = new JwtServiceImpl(otherSecretKey, accessTokenExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) otherJwtService).init();
        String token = otherJwtService.generateAccessToken(userId);
        assertFalse(jwtService.isTokenValid(token, userId));
    }

    @Test
    void isTokenValid_shouldReturnFalseForNullUserId() {
        String token = jwtService.generateAccessToken(userId);
        assertFalse(jwtService.isTokenValid(token, null));
    }

    @Test
    void extractClaim_shouldReturnCorrectClaim() {
        String token = jwtService.generateAccessToken(userId);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals(userId.toString(), subject);

        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractAllClaims_shouldReturnAllClaims() {
        String token = jwtService.generateAccessToken(userId);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals(userId.toString(), subject);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
    }
}
