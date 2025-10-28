package pt.estga.stonemark.services.content;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.repositories.content.MarkRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkServiceHibernateImpl implements MarkService {

    private final MarkRepository repository;

    @Override
    public Page<Mark> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Mark> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Mark create(Mark mark) {
        return repository.save(mark);
    }

    @Override
    public Mark update(Mark mark) {
        return repository.save(mark);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
