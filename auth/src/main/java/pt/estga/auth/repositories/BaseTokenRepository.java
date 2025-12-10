package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pt.estga.auth.entities.token.BaseToken;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
// Todo: get rid of it
public interface BaseTokenRepository<T extends BaseToken> extends JpaRepository<T, Long> {

    Optional<T> findByToken(String token);

    List<T> findAllByUser(User user);

}
