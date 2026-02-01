package pt.estga.content.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.events.MarkCreatedEvent;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.services.MarkService;
import pt.estga.file.entities.MediaFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkServiceHibernateImpl implements MarkService {

    private final MarkRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Mark> findById(Long id) {
        return repository.findById(id);
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
        Mark savedMark = repository.save(mark);
        if (savedMark.getCover() != null) {
            eventPublisher.publishEvent(new MarkCreatedEvent(this, savedMark.getId(), savedMark.getCover().getId(), savedMark.getCover().getOriginalFilename()));
        }
        return savedMark;
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
        Mark savedMark = repository.save(mark);
        if (savedMark.getCover() != null) {
            eventPublisher.publishEvent(new MarkCreatedEvent(this, savedMark.getId(), savedMark.getCover().getId(), savedMark.getCover().getOriginalFilename()));
        }
        return savedMark;
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
