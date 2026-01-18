package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.content.entities.Mark;

import java.io.IOException;
import java.util.Optional;

public interface MarkService {

    Page<Mark> findAll(Pageable pageable);

    Optional<Mark> findById(Long id);

    Optional<Mark> findWithCoverById(Long id);

    long count();

    Mark create(Mark mark);

    Mark create(Mark mark, MultipartFile file) throws IOException;

    Mark update(Mark mark);

    Mark update(Mark mark, MultipartFile file) throws IOException;

    void deleteById(Long id);

    Mark updateCover(Long id, MultipartFile file) throws IOException;
}
