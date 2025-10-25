package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.Monument;

@Repository
public interface MonumentRepository extends JpaRepository<Monument, Long> {
}
