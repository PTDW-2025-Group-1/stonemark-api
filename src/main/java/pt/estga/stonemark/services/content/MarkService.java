package pt.estga.stonemark.services.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.content.Mark;

import java.util.Optional;

public interface MarkService {

    Page<Mark> findAll(Pageable pageable);

    Optional<Mark> findById(Long id);

    Mark create(Mark mark);

    Mark update(Mark mark);

    void deleteById(Long id);

}
