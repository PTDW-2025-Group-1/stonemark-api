package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.auth.entities.request.TelephoneChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;

import java.util.Optional;

public interface TelephoneChangeRequestRepository extends JpaRepository<TelephoneChangeRequest, Long> {

    Optional<TelephoneChangeRequest> findByVerificationToken(VerificationToken token);

}
