package pt.estga.stonemark.services.content.arquived;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.content.Mason;
import pt.estga.stonemark.repositories.content.MasonRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MasonServiceHibernateImpl implements MasonService {

    private final MasonRepository repository;

    @Override
    public Page<Mason> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Mason> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Mason create(Mason mason) {
        return repository.save(mason);
    }

    @Override
    public Mason update(Mason mason) {
        return repository.save(mason);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
