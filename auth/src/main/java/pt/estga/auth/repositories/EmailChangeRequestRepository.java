package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.auth.entities.request.EmailChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;

import java.util.Optional;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, String> {

    Optional<EmailChangeRequest> findByVerificationToken(VerificationToken token);
}
