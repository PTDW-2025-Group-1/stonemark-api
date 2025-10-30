package pt.estga.stonemark.services.auth;

import pt.estga.stonemark.entities.token.BaseToken;
import pt.estga.stonemark.repositories.token.BaseTokenRepository;

import java.time.Instant;
import java.util.Optional;

public abstract class BaseTokenServiceImpl<T extends BaseToken, R extends BaseTokenRepository<T>> implements BaseTokenService<T> {

    private final R repository;

    public BaseTokenServiceImpl(R repository) {
        this.repository = repository;
    }

    protected R getRepository() {
        return repository;
    }

    @Override
    public Optional<T> findByToken(String token) {
        return repository.findByToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return findByToken(token)
                .map(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public void revokeToken(String token) {
        findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }
}
