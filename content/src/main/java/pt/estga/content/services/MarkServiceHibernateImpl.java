package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkServiceHibernateImpl implements MarkService {

    private final MarkRepository repository;
    private final MediaService mediaService;
    private final DetectionService detectionService;

    @Override
    public Page<Mark> findAll(Pageable pageable) {
        return repository.findByActiveIsTrue(pageable);
    }

    @Override
    public Page<Mark> findAllManagement(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Mark> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Mark> findWithCoverById(Long id) {
        return repository.findWithCoverById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    @Transactional
    public Mark create(Mark mark) {
        return create(mark, null);
    }

    @Override
    @Transactional
    public Mark create(Mark mark, MediaFile cover) {
        if (cover != null) {
            mark.setCover(cover);
        }
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    @Transactional
    public Mark update(Mark mark) {
        return update(mark, null);
    }

    @Override
    @Transactional
    public Mark update(Mark mark, MediaFile cover) {
        if (cover != null) {
            mark.setCover(cover);
        }
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private void processEmbeddingIfCoverExists(Mark mark) {
        if (mark.getCover() != null) {
            try {
                // We need to reload the file resource to read it again for detection
                // This assumes the file is accessible via MediaService
                var resource = mediaService.loadFileById(mark.getCover().getId());
                DetectionResult detectionResult = detectionService.detect(resource.getInputStream(), mark.getCover().getOriginalFilename());
                
                if (detectionResult.isMasonMark()) {
                    mark.setEmbedding(detectionResult.embedding());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error processing image for embedding", e);
            }
        }
    }
}
