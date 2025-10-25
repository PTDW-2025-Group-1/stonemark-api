package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.Token;
import pt.estga.stonemark.respositories.TokenRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceHibernateImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Override
    public List<Token> findAllValidByUser(Long userId) {
        return tokenRepository.findAllByUserIdAndExpiredFalseAndRevokedFalse(userId);
    }

    @Override
    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Override
    public Token save(Token token) {
        return tokenRepository.save(token);
    }

    @Override
    public List<Token> saveAll(List<Token> tokens) {
        return tokenRepository.saveAll(tokens);
    }

    @Override
    public boolean deleteAllRevokedAndExpiredTokens() {
        try {
            tokenRepository.deleteByExpiredTrueAndRevokedTrue();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
