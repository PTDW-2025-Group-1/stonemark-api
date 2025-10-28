package pt.estga.stonemark.services.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.Monument;

import java.util.Optional;

public interface MonumentService {

    Page<Monument> findAll(Pageable pageable);

    Optional<Monument> findById(Long id);

    Monument create(Monument monument);

    Monument update(Monument monument);

    void deleteById(Long id);

}
