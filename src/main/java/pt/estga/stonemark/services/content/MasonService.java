package pt.estga.stonemark.services.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.Mason;

import java.util.Optional;

public interface MasonService {

    Page<Mason> findAll(Pageable pageable);

    Optional<Mason> findById(Long id);

    Mason create(Mason mason);

    Mason update(Mason mason);

    void deleteById(Long id);

}
