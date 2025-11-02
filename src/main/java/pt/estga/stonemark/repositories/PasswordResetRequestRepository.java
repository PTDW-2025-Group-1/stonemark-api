package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.request.PasswordResetRequest;
import pt.estga.stonemark.entities.token.VerificationToken;

import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    Optional<PasswordResetRequest> findByVerificationToken(VerificationToken verificationToken);
}