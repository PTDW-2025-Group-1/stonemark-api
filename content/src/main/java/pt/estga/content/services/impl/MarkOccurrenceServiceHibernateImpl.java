package pt.estga.content.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.events.MarkOccurrenceCreatedEvent;
import pt.estga.content.repositories.MarkOccurrenceRepository;
import pt.estga.content.services.MarkOccurrenceService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceServiceHibernateImpl implements MarkOccurrenceService {

    private final MarkOccurrenceRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final MediaService mediaService;

    @Override
    public Optional<MarkOccurrence> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public MarkOccurrence create(MarkOccurrence occurrence, MultipartFile file, Long coverId) throws IOException {
        MediaFile mediaFile = null;

        if (file != null && !file.isEmpty()) {
            mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        } else if (coverId != null) {
            mediaFile = mediaService.findById(coverId).orElse(null);
        }

        if (mediaFile != null) {
            occurrence.setCover(mediaFile);
        }

        MarkOccurrence savedOccurrence = repository.save(occurrence);
        if (savedOccurrence.getCover() != null) {
            eventPublisher.publishEvent(new MarkOccurrenceCreatedEvent(this, savedOccurrence.getId(), savedOccurrence.getCover().getId(), savedOccurrence.getCover().getOriginalFilename()));
        }
        return savedOccurrence;
    }

    @Override
    @Transactional
    public MarkOccurrence update(MarkOccurrence occurrence, MultipartFile file, Long coverId) throws IOException {
        MediaFile mediaFile = null;

        if (file != null && !file.isEmpty()) {
            mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        } else if (coverId != null) {
            mediaFile = mediaService.findById(coverId).orElse(null);
        }

        if (mediaFile != null) {
            occurrence.setCover(mediaFile);
        }

        MarkOccurrence savedOccurrence = repository.save(occurrence);
        if (savedOccurrence.getCover() != null) {
            eventPublisher.publishEvent(new MarkOccurrenceCreatedEvent(this, savedOccurrence.getId(), savedOccurrence.getCover().getId(), savedOccurrence.getCover().getOriginalFilename()));
        }
        return savedOccurrence;
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
