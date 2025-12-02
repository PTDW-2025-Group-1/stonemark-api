package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.user.entities.User;
import pt.estga.user.entities.VerificationCode;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserAndNewTelephone(User user, String newTelephone);
    Optional<VerificationCode> findByUserAndNewTelephoneAndCode(User user, String newTelephone, String code);
    List<VerificationCode> findAllByUserAndUsedFalse(User user);
}
