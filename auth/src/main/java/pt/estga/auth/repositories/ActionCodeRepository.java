package pt.estga.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.user.entities.User;

import java.util.Optional;

@Repository
public interface ActionCodeRepository extends JpaRepository<ActionCode, String> {

    Optional<ActionCode> findByUserAndType(User user, ActionCodeType type);

    void deleteByUserAndType(User user, ActionCodeType type);
}
