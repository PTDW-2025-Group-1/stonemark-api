package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;
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
    public Optional<MarkOccurrence> findByIdWithMonument(Long id) {
        return repository.findByIdWithMonument(id);
    }

    @Override
    public Page<MarkOccurrence> findByMarkId(Long markId, Pageable pageable) {
        return repository.findAllByMarkId(markId, pageable);
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

    public Page<MarkOccurrence> findByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable) {
        return repository.findAllByMarkIdAndMonumentId(markId, monumentId, pageable);
    }

    @Override
    public List<Mark> findAvailableMarksByMonumentId(Long monumentId) {
        return repository.findDistinctMarksByMonumentId(monumentId);
    }

    public List<Monument> findAvailableMonumentsByMarkId(Long markId) {
        return repository.findDistinctMonumentsByMarkId(markId);
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
