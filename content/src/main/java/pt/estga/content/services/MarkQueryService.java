package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Mark;

import java.util.Optional;

public interface MarkQueryService {

    Page<Mark> findAll(Pageable pageable);

    Page<Mark> findAllManagement(Pageable pageable);

    Optional<Mark> findById(Long id);

    Optional<Mark> findWithCoverById(Long id);

    long count();
}
