package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.MarkOccurrence;

import java.util.Optional;

public interface MarkOccurrenceService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    MarkOccurrence create(MarkOccurrence occurrence);

    MarkOccurrence update(MarkOccurrence occurrence);

    void deleteById(Long id);

}
