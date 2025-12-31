package pt.estga.security.services;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

@Deprecated
public interface JwtService {

    Long getUserIdFromToken(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateAccessToken(Long userId);

    String generateRefreshToken(Long userId);

    Boolean isTokenValid(String token, Long userId);

}
