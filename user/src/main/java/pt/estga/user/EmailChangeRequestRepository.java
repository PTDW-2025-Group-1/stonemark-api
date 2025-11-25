package pt.estga.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;

import java.util.Optional;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, String> {

    Optional<EmailChangeRequest> findByVerificationToken(VerificationToken token);
}
