package pt.estga.stonemark.repositories.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.Mason;

@Repository
public interface MasonRepository extends JpaRepository<Mason, Long> {
}
