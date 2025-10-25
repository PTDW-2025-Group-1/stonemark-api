package pt.estga.stonemark.services;

import pt.estga.stonemark.entities.Token;

import java.util.List;
import java.util.Optional;

public interface TokenService {

    List<Token> findAllValidByUser(Long userId);

    Optional<Token> findByToken(String token);

    Token save(Token token);

    List<Token> saveAll(List<Token> tokens);

    boolean deleteAllRevokedAndExpiredTokens();

}
