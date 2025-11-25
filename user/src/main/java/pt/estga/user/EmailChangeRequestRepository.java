package pt.estga.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.user.entities.request.EmailChangeRequest;

import java.util.Optional;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, String> {

    Optional<EmailChangeRequest> findByVerificationToken(VerificationToken token);
}
