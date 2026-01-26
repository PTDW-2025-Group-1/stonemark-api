package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Mark;
import pt.estga.file.entities.MediaFile;

import java.util.Optional;

public interface MarkService {

    Page<Mark> findAll(Pageable pageable);

    Page<Mark> findAllManagement(Pageable pageable);

    Optional<Mark> findById(Long id);

    Optional<Mark> findWithCoverById(Long id);

    long count();

    Mark create(Mark mark);

    Mark create(Mark mark, MediaFile cover);

    Mark update(Mark mark);

    Mark update(Mark mark, MediaFile cover);

    void deleteById(Long id);
}
