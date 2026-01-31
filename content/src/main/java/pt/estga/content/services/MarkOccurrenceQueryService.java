package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceQueryService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Page<MarkOccurrence> findAllManagement(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    Page<MarkOccurrence> findByMarkId(Long markId, Pageable pageable);

    List<MarkOccurrence> findByMarkIdForMap(Long markId);

    List<MarkOccurrence> findLatest(int limit);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    Page<MarkOccurrence> findByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable);

    List<Mark> findAvailableMarksByMonumentId(Long monumentId);

    List<Monument> findAvailableMonumentsByMarkId(Long markId);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);

    long countDistinctMonumentsByMarkId(Long markId);
}
