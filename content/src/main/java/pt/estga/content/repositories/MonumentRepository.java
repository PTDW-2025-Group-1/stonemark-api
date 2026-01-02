package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Monument;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonumentRepository extends JpaRepository<Monument, Long> {
    Optional<Monument> findByName(String name);

    @Query("SELECT m FROM Monument m WHERE m.name LIKE %:name% AND m.active = true")
    List<Monument> findByNameContaining(@Param("name") String name);

    List<Monument> findByLatitudeBetweenAndLongitudeBetween(double minLat, double maxLat, double minLon, double maxLon);

    Optional<Monument> findByLatitudeAndLongitude(double latitude, double longitude);

    Page<Monument> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Monument> findByCityIgnoreCaseAndActiveTrue(String city, Pageable pageable);

    Page<Monument> findByActiveTrue(Pageable pageable);

}
