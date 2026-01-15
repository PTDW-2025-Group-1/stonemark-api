package pt.estga.content.repositories;

import org.locationtech.jts.geom.Geometry;
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

    Page<Monument> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Monument> findByActiveTrue(Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_Within(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), ST_GeomFromGeoJSON(:geoJson)) AND m.active = true", nativeQuery = true)
    Page<Monument> findByPolygon(@Param("geoJson") String geoJson, Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_Within(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), :geometry) AND m.active = true", nativeQuery = true)
    Page<Monument> findByGeometry(@Param("geometry") Geometry geometry, Pageable pageable);

    @Query(value = "SELECT * FROM monument m WHERE ST_Within(ST_SetSRID(ST_Point(m.longitude, m.latitude), 4326), ST_MakeEnvelope(:longitude1, :latitude1, :longitude2, :latitude2, 4326))", nativeQuery = true)
    List<Monument> findByLatitudeBetweenAndLongitudeBetween(@Param("latitude1") double latitude1, @Param("latitude2") double latitude2, @Param("longitude1") double longitude1, @Param("longitude2") double longitude2);
}
