package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
        return repository.findAllWithCover(pageable);
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
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    @Transactional
    public Mark create(Mark mark, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            mark.setCover(mediaFile);
        }
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    @Transactional
    public Mark update(Mark mark) {
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    @Transactional
    public Mark update(Mark mark, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            mark.setCover(mediaFile);
        }
        processEmbeddingIfCoverExists(mark);
        return repository.save(mark);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public Mark updateCover(Long id, MultipartFile file) throws IOException {
        Mark mark = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mark not found"));

        MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        mark.setCover(mediaFile);
        processEmbeddingIfCoverExists(mark);
        
        return repository.save(mark);
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
