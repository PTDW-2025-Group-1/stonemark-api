package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.repositories.MarkOccurrenceRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceServiceHibernateImpl implements MarkOccurrenceService {

    private final MarkOccurrenceRepository repository;

    @Override
    public Page<MarkOccurrence> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<MarkOccurrence> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public MarkOccurrence create(MarkOccurrence occurrence) {
        return repository.save(occurrence);
    }

    @Override
    public MarkOccurrence update(MarkOccurrence occurrence) {
        return repository.save(occurrence);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
