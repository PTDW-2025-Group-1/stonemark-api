package pt.estga.stonemark.services.content;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.content.MarkOccurrence;
import pt.estga.stonemark.repositories.content.MarkOccurrenceRepository;

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
    public MarkOccurrence create(MarkOccurrence markOccurrence) {
        return repository.save(markOccurrence);
    }

    @Override
    public MarkOccurrence update(MarkOccurrence markOccurrence) {
        return repository.save(markOccurrence);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
