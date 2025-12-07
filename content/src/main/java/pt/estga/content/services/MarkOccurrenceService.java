package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.MarkOccurrence;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    List<MarkOccurrence> findByMarkId(Long markId);

    List<MarkOccurrence> findLatest(int limit);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    long countByMonumentId(Long monumentId);

    MarkOccurrence create(MarkOccurrence occurrence);

    MarkOccurrence update(MarkOccurrence occurrence);

    void deleteById(Long id);

}
