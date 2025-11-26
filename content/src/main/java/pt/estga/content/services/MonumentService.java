package pt.estga.content.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.content.entities.Monument;

import java.util.List;
import java.util.Optional;

public interface MonumentService {

    Page<Monument> findAll(Pageable pageable);

    Optional<Monument> findById(Long id);

    Optional<Monument> findByName(String name);

    List<Monument> findByNameContaining(String name);

    Optional<Monument> findByLatitudeAndLongitude(double latitude, double longitude);

    List<Monument> findByCoordinatesInRange(double latitude, double longitude, double range);

    List<Monument> findLatest(int limit);

    long count();

    Page<Monument> searchByName(String query, Pageable pageable);

    Page<Monument> findByCity(String city, Pageable pageable);

    Monument create(Monument monument);

    Monument update(Monument monument);

    void deleteById(Long id);

}
