package pt.estga.security.services;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateAccessToken(String username);

    String generateRefreshToken(String username);

    Boolean isTokenValid(String token, String username);

}
