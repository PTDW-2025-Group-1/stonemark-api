package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.MarkOccurrence;

import java.util.List;

@Repository
public interface MarkOccurrenceRepository extends JpaRepository<MarkOccurrence, Long> {
    List<MarkOccurrence> findAllByMarkId(Long markId);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);
}
