package pt.estga.content.services;

import org.springframework.web.multipart.MultipartFile;
import pt.estga.content.entities.MarkOccurrence;

import java.io.IOException;
import java.util.Optional;

public interface MarkOccurrenceService {

    Optional<MarkOccurrence> findById(Long id);

    MarkOccurrence create(MarkOccurrence occurrence, MultipartFile file, Long coverId) throws IOException;

    MarkOccurrence update(MarkOccurrence occurrence, MultipartFile file, Long coverId) throws IOException;

    void deleteById(Long id);
}
