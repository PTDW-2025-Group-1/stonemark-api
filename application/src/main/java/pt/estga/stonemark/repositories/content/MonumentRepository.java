package pt.estga.stonemark.repositories.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.Monument;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonumentRepository extends JpaRepository<Monument, Long> {
    Optional<Monument> findByName(String name);

    @Query("SELECT m FROM Monument m WHERE m.name LIKE %:name%")
    List<Monument> findByNameContaining(@Param("name") String name);

    @Query("SELECT m FROM Monument m WHERE m.latitude BETWEEN :lat - :range AND :lat + :range AND m.longitude BETWEEN :lon - :range AND :lon + :range")
    List<Monument> findByCoordinatesInRange(@Param("lat") double latitude, @Param("lon") double longitude, @Param("range") double range);

    Optional<Monument> findByLatitudeAndLongitude(double latitude, double longitude);
}
