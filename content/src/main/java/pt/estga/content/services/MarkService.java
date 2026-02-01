package pt.estga.content.services;

import pt.estga.content.entities.Mark;
import pt.estga.file.entities.MediaFile;

import java.util.Optional;

public interface MarkService {

    Optional<Mark> findById(Long id);

    Mark create(Mark mark);

    Mark create(Mark mark, MediaFile cover);

    Mark update(Mark mark);

    Mark update(Mark mark, MediaFile cover);

    void deleteById(Long id);

}
