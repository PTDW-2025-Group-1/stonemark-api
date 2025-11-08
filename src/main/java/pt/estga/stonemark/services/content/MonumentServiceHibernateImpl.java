package pt.estga.stonemark.services.content;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.repositories.content.MonumentRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonumentServiceHibernateImpl implements MonumentService {

    private final MonumentRepository repository;

    @Override
    public Page<Monument> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Monument> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Monument create(Monument monument) {
        return repository.save(monument);
    }

    @Override
    public Monument update(Monument monument) {
        return repository.save(monument);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
