package pt.estga.content.services.arquived;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Guild;
import pt.estga.content.repositories.GuildRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuildServiceHibernateImpl implements GuildService {

    private final GuildRepository repository;

    @Override
    public Page<Guild> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Guild> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Guild create(Guild guild) {
        return repository.save(guild);
    }

    @Override
    public Guild update(Guild guild) {
        return repository.save(guild);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
