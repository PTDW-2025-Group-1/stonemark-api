package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceService {

    Page<MarkOccurrence> findAll(Pageable pageable);

    Optional<MarkOccurrence> findById(Long id);

    Page<MarkOccurrence> findByMarkId(Long markId, Pageable pageable);

    List<MarkOccurrence> findByMarkIdForMap(Long markId);

    List<MarkOccurrence> findLatest(int limit);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    Page<MarkOccurrence> findByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable);

    List<Mark> findAvailableMarksByMonumentId(Long monumentId);

    List<Monument> findAvailableMonumentsByMarkId(Long markId);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);

    long countDistinctMonumentsByMarkId(Long markId);

    MarkOccurrence create(MarkOccurrence occurrence);

    MarkOccurrence create(MarkOccurrence occurrence, MultipartFile file) throws IOException;

    MarkOccurrence update(MarkOccurrence occurrence);

    MarkOccurrence update(MarkOccurrence occurrence, MultipartFile file) throws IOException;

    void deleteById(Long id);

    MarkOccurrence updateCover(Long id, MultipartFile file) throws IOException;
}
