package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

}
