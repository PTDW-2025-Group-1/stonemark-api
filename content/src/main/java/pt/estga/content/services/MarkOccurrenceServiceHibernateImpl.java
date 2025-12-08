package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.repositories.MarkOccurrenceRepository;

import java.util.List;
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
    public List<MarkOccurrence> findByMarkId(Long markId) {
        return repository.findAllByMarkId(markId);
    }

    @Override
    public List<MarkOccurrence> findLatest(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(pageable).getContent();
    }

    @Override
    public Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable) {
        return repository.findByMonumentId(monumentId, pageable);
    }

    @Override
    public long countByMonumentId(Long monumentId) {
        return repository.countByMonumentId(monumentId);
    }

    @Override
    public long countByMarkId(Long markId) {
        return repository.countByMarkId(markId);
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
