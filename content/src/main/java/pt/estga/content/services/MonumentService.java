package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Monument;

import java.util.List;
import java.util.Optional;

public interface MonumentService {

    Page<Monument> findAll(Pageable pageable);

    Optional<Monument> findById(Long id);

    List<Monument> findByCoordinatesInRange(double latitude, double longitude, double range);

    List<Monument> findLatest(int limit);

    long count();

    Page<Monument> searchByName(String query, Pageable pageable);

    Page<Monument> findByPolygon(String geoJson, Pageable pageable);

    Page<Monument> findByDivisionId(Long id, Pageable pageable);

    Monument create(Monument monument);

    Monument update(Monument monument);

    void deleteById(Long id);

    List<Monument> findPopular(int limit);
}
