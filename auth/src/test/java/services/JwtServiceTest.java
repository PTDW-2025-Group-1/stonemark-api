package services;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.JwtServiceImpl;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long accessTokenExpiration = 1000 * 60 * 24; // 24 hours
    private final long refreshTokenExpiration = 1000 * 60 * 24 * 7; // 7 days

    @BeforeEach
    void setUp() {
        userDetails = new User("testuser", "password", Collections.emptyList());
        jwtService = new JwtServiceImpl(secretKey, accessTokenExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) jwtService).init(); // Manually call PostConstruct
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateAccessToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void extractUsername_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.string";
        String username = jwtService.extractUsername(invalidToken);
        assertNull(username);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtService.isTokenValid(invalidToken, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        long shortExpiration = 10; // 10 milliseconds
        JwtService shortLivedJwtService = new JwtServiceImpl(secretKey, shortExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) shortLivedJwtService).init(); // Manually call PostConstruct
        String token = shortLivedJwtService.generateAccessToken(userDetails);

        Thread.sleep(50); // Wait for the token to expire

        assertFalse(shortLivedJwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTokenSignedWithDifferentKey() {
        String otherSecretKey = "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";
        JwtService otherJwtService = new JwtServiceImpl(otherSecretKey, accessTokenExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) otherJwtService).init();
        String token = otherJwtService.generateAccessToken(userDetails);
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForNullUserDetails() {
        String token = jwtService.generateAccessToken(userDetails);
        assertFalse(jwtService.isTokenValid(token, null));
    }

    @Test
    void extractClaim_shouldReturnCorrectClaim() {
        String token = jwtService.generateAccessToken(userDetails);
        String username = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals("testuser", username);

        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractAllClaims_shouldReturnAllClaims() {
        String token = jwtService.generateAccessToken(userDetails);
        String username = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals("testuser", username);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
    }
}
