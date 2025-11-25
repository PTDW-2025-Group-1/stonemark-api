package pt.estga.stonemark.repositories.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.MarkOccurrence;

@Repository
public interface MarkOccurrenceRepository extends JpaRepository<MarkOccurrence, Long> {
}
