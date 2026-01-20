package pt.estga.content.repositories;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"district", "parish", "municipality"})
    Optional<Monument> findById(Long id);

    @Query("SELECT m FROM Monument m " +
            "LEFT JOIN FETCH m.district " +
            "LEFT JOIN FETCH m.parish " +
            "LEFT JOIN FETCH m.municipality " +
            "WHERE m.active = true")
    Page<Monument> findAllWithDivisions(Pageable pageable);

    @Query("SELECT m FROM Monument m " +
            "LEFT JOIN FETCH m.district " +
            "LEFT JOIN FETCH m.parish " +
            "LEFT JOIN FETCH m.municipality ")
    Page<Monument> findAllWithDivisionsAdmin(Pageable pageable);

    Optional<Monument> findByExternalId(String externalId);

    Page<Monument> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Monument> findByActiveTrue(Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_Within(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), ST_GeomFromGeoJSON(:geoJson)) AND m.active = true", nativeQuery = true)
    Page<Monument> findByPolygon(@Param("geoJson") String geoJson, Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_Within(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), :geometry) AND m.active = true", nativeQuery = true)
    Page<Monument> findByGeometry(@Param("geometry") Geometry geometry, Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_DWithin(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), ST_SetSRID(ST_Point(:longitude, :latitude), 4326), :range) AND m.active = true", nativeQuery = true)
    List<Monument> findByCoordinatesInRange(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("range") double range);

    @Query("SELECT m FROM MarkOccurrence mo JOIN mo.monument m WHERE m.active = true GROUP BY m ORDER BY COUNT(m) DESC")
    List<Monument> findPopular(Pageable pageable);
}
