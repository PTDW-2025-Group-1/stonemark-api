package pt.estga.security.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private JwtParser jwtParser;

    public JwtServiceImpl(
            @Value("${application.security.jwt.secret-key}") String secretKey,
            @Value("${application.security.jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${application.security.jwt.refresh-token.expiration}") long refreshTokenExpiration) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @PostConstruct
    public void init() {
        jwtParser = Jwts.parser().verifyWith(getSigningKey()).build();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String subject = claims.getSubject();
        if (subject == null) {
            return null;
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claims == null ? null : claimsResolver.apply(claims);
    }

    @Override
    public String generateAccessToken(Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        return buildToken(extraClaims, userId, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        return buildToken(extraClaims, userId, refreshTokenExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            Long userId,
            long expiration
    ) {
        long current = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId.toString())
                .issuedAt(new Date(current))
                .expiration(new Date(current + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Boolean isTokenValid(String token, Long userId) {
        if (userId == null) {
            return false;
        }
        final Long extractedUserId = getUserIdFromToken(token);
        return extractedUserId != null && (extractedUserId.equals(userId)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    private SecretKey getSigningKey() {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}
