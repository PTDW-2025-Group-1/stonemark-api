package pt.estga.stonemark.services.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.MarkOccurrence;

import java.util.Optional;

public interface MarkOccurrenceService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    MarkOccurrence create(MarkOccurrence markOccurrence);

    MarkOccurrence update(MarkOccurrence markOccurrence);

    void deleteById(Long id);

}
