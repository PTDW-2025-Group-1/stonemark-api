package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Mark;

import java.util.Optional;

public interface MarkService {

    Page<Mark> findAll(Pageable pageable);

    Optional<Mark> findById(Long id);

    Optional<Mark> findWithCoverById(Long id);

    long count();

    Page<Mark> searchByTitle(String title, Pageable pageable);

    Mark create(Mark mark);

    Mark update(Mark mark);

    void deleteById(Long id);

}
