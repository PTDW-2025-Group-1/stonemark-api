package pt.estga.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.MarkOccurrence;

@Repository
public interface MarkOccurrenceRepository extends JpaRepository<MarkOccurrence, Long> {
}
