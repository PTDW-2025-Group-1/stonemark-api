package pt.estga.content.services;

import pt.estga.content.entities.Monument;

import java.util.Optional;

public interface MonumentService {

    Optional<Monument> findById(Long id);

    Monument create(Monument monument);

    Monument update(Monument monument);

    void deleteById(Long id);

    void enrichWithDivisions(Monument monument);

}
