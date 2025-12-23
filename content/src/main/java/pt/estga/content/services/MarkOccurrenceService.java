package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    Optional<MarkOccurrence> findByIdWithRelationships(Long id);

    Page<MarkOccurrence> findByMarkId(Long markId, Pageable pageable);

    List<MarkOccurrence> findLatest(int limit);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    Page<MarkOccurrence> findByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable);

    List<Mark> findAvailableMarksByMonumentId(Long monumentId);

    List<Monument> findAvailableMonumentsByMarkId(Long markId);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);

    MarkOccurrence create(MarkOccurrence occurrence);

    MarkOccurrence update(MarkOccurrence occurrence);

    void deleteById(Long id);

}
