package pt.estga.stonemark.repositories.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pt.estga.stonemark.entities.token.BaseToken;

import java.util.Optional;

@NoRepositoryBean
public interface BaseTokenRepository<T extends BaseToken> extends JpaRepository<T, Long> {

    Optional<T> findByToken(String token);
}
