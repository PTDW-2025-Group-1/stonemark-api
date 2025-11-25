package pt.estga.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Mason;

@Repository
public interface MasonRepository extends JpaRepository<Mason, Long> {
}
