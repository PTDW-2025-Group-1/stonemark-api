package pt.estga.security.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import pt.estga.security.enums.TokenType;
import pt.estga.shared.enums.PrincipalType;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewJwtServiceImpl implements NewJwtService {

    private static final Logger logger = LoggerFactory.getLogger(NewJwtServiceImpl.class);

    private static final String PID_CLAIM = "pid";
    private static final String TYPE_CLAIM = "type";
    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private JwtParser jwtParser;

    public NewJwtServiceImpl(
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
    public PrincipalType getPrincipalType(String token) {
        try {
            String type = extractAllClaims(token).get(TYPE_CLAIM, String.class);
            if (type == null) return null;
            return PrincipalType.valueOf(type);
        } catch (Exception e) {
            logger.error("Error extracting principal type from token", e);
            return null;
        }
    }

    @Override
    public Long getPrincipalId(String token) {
        try {
            Object pidObj = extractAllClaims(token).get(PID_CLAIM);
            if (pidObj instanceof Number) {
                return ((Number) pidObj).longValue();
            }
            return null;
        } catch (Exception e) {
            logger.error("Error extracting principal ID from token", e);
            return null;
        }
    }

    @Override
    public String getSubject(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            logger.error("Error extracting subject from token", e);
            return null;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        try {
            List<?> roles = extractAllClaims(token).get(ROLES_CLAIM, List.class);
            if (roles == null) return List.of();
            
            return roles.stream()
                    .filter(obj -> obj instanceof String)
                    .map(obj -> (String) obj)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error extracting authorities from token", e);
            return List.of();
        }
    }

    @Override
    public boolean isTokenValid(String token, TokenType expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                logger.warn("Token expired");
                return false;
            }
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                logger.warn("Token subject is missing or blank");
                return false;
            }
            boolean typeMatches = expectedType.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
            if (!typeMatches) {
                logger.warn("Token type mismatch. Expected: {}, Actual: {}", expectedType, claims.get(TOKEN_TYPE_CLAIM));
            }
            return typeMatches;
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    @Override
    public String generateAccessToken(PrincipalType type, Long principalId, String identifier, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(PID_CLAIM, principalId);
        claims.put(TYPE_CLAIM, type.name());
        claims.put(TOKEN_TYPE_CLAIM, TokenType.ACCESS.name());
        claims.put(ROLES_CLAIM, authorities.stream().map(GrantedAuthority::getAuthority).toList());
        return buildToken(claims, identifier, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(PrincipalType type, Long principalId, String identifier) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(PID_CLAIM, principalId);
        claims.put(TYPE_CLAIM, type.name());
        claims.put(TOKEN_TYPE_CLAIM, TokenType.REFRESH.name());
        return buildToken(claims, identifier, refreshTokenExpiration);
    }

    @Override
    public Map<TokenType, String> generateTokens(PrincipalType type, Long principalId, String identifier, Collection<? extends GrantedAuthority> authorities) {
        Map<TokenType, String> tokens = new HashMap<>();
        tokens.put(TokenType.ACCESS, generateAccessToken(type, principalId, identifier, authorities));
        tokens.put(TokenType.REFRESH, generateRefreshToken(type, principalId, identifier));
        return tokens;
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}