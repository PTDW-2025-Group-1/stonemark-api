import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pt.estga.security.enums.TokenType;
import pt.estga.security.services.JwtService;
import pt.estga.security.services.JwtServiceImpl;
import pt.estga.shared.enums.PrincipalType;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final Long userId = 123L;
    private final String username = "testuser";
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
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Long extractedUserId = jwtService.getPrincipalId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUserId_FromToken_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.string";
        Long extractedUserId = jwtService.getPrincipalId(invalidToken);
        assertNull(extractedUserId);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, TokenType.ACCESS));
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(PrincipalType.USER, userId, username);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, TokenType.REFRESH));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(jwtService.isTokenValid(token, TokenType.ACCESS));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtService.isTokenValid(invalidToken, TokenType.ACCESS));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        long shortExpiration = 10; // 10 milliseconds
        JwtService shortLivedJwtService = new JwtServiceImpl(secretKey, shortExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) shortLivedJwtService).init(); // Manually call PostConstruct
        String token = shortLivedJwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        Thread.sleep(50); // Wait for the token to expire

        assertFalse(shortLivedJwtService.isTokenValid(token, TokenType.ACCESS));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTokenSignedWithDifferentKey() {
        String otherSecretKey = "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";
        JwtService otherJwtService = new JwtServiceImpl(otherSecretKey, accessTokenExpiration, refreshTokenExpiration);
        ((JwtServiceImpl) otherJwtService).init();
        String token = otherJwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertFalse(jwtService.isTokenValid(token, TokenType.ACCESS));
    }

    @Test
    void getPrincipalType_shouldReturnCorrectType() {
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(PrincipalType.USER, jwtService.getPrincipalType(token));
    }

    @Test
    void getSubject_shouldReturnCorrectSubject() {
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(username, jwtService.getSubject(token));
    }

    @Test
    void getAuthorities_shouldReturnCorrectAuthorities() {
        String token = jwtService.generateAccessToken(PrincipalType.USER, userId, username, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        var authorities = jwtService.getAuthorities(token);
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }
}