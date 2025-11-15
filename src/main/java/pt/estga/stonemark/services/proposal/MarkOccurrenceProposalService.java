package pt.estga.stonemark.services.proposal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.MarkOccurrence;

import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    MarkOccurrence create(MarkOccurrence occurrence);

    void deleteById(Long id);

}
