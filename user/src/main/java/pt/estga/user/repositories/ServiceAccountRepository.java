package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.user.entities.ServiceAccount;

import java.util.Optional;

@Repository
public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Long> {
    Optional<ServiceAccount> findByName(String name);
}
