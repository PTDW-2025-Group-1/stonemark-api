package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.auth.entities.TwoFactorCode;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.user.entities.User;

import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, String> {

    Optional<TwoFactorCode> findByUserAndPurpose(User user, VerificationTokenPurpose purpose);

    void deleteByUserAndPurpose(User user, VerificationTokenPurpose purpose);
}
