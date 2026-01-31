package pt.estga.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Monument;

import java.util.Optional;

@Repository
public interface MonumentRepository extends JpaRepository<Monument, Long> {

    Optional<Monument> findByExternalId(String externalId);

}
