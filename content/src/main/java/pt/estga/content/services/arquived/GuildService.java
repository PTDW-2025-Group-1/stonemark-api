package pt.estga.stonemark.services.content.arquived;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.Guild;

import java.util.Optional;

public interface GuildService {

    Page<Guild> findAll(Pageable pageable);

    Optional<Guild> findById(Long id);

    Guild create(Guild guild);

    Guild update(Guild guild);

    void deleteById(Long id);

}
