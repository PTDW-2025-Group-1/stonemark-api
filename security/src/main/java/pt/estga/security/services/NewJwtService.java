package pt.estga.security.services;

import org.springframework.security.core.GrantedAuthority;
import pt.estga.security.enums.TokenType;
import pt.estga.shared.enums.PrincipalType;

import java.util.Collection;
import java.util.Map;

public interface NewJwtService {

    String generateAccessToken(PrincipalType type, Long principalId, String identifier, Collection<? extends GrantedAuthority> authorities);

    String generateRefreshToken(PrincipalType type, Long principalId, String identifier);

    Map<TokenType, String> generateTokens(PrincipalType type, Long principalId, String identifier, Collection<? extends GrantedAuthority> authorities);

    PrincipalType getPrincipalType(String token);

    Long getPrincipalId(String token);

    String getSubject(String token);

    Collection<? extends GrantedAuthority> getAuthorities(String token);

    boolean isTokenValid(String token, TokenType expectedType);

}
