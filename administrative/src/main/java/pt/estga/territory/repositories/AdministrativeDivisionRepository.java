package pt.estga.territory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.LogicalLevel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdministrativeDivisionRepository extends JpaRepository<AdministrativeDivision, String> {
    List<AdministrativeDivision> findAllByNameIn(Collection<String> names);
    List<AdministrativeDivision> findByOsmAdminLevel(int osmAdminLevel);
    List<AdministrativeDivision> findByLogicalLevel(LogicalLevel logicalLevel);
    
    // Changed to findByParent_Id to correctly map to the parent's id property
    List<AdministrativeDivision> findByParent_Id(String parentId);

    @Query(value = "SELECT c.* FROM administrative_division c " +
            "JOIN administrative_division p ON ST_Intersects(c.geometry, p.geometry) " +
            "WHERE p.id = :parentId AND c.osm_admin_level = :childLevel", nativeQuery = true)
    List<AdministrativeDivision> findChildrenByGeometry(@Param("parentId") String parentId, @Param("childLevel") int childLevel);

    @Query(value = "SELECT p.* FROM administrative_division p " +
            "JOIN administrative_division c ON ST_Intersects(p.geometry, c.geometry) " +
            "WHERE c.id = :childId AND p.osm_admin_level = :parentLevel " +
            "ORDER BY ST_Area(ST_Intersection(p.geometry, c.geometry)) DESC LIMIT 1", nativeQuery = true)
    Optional<AdministrativeDivision> findParentByGeometry(@Param("childId") String childId, @Param("parentLevel") int parentLevel);

    @Query(value = "SELECT * FROM administrative_division d " +
            "WHERE ST_Contains(d.geometry, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)) " +
            "ORDER BY d.osm_admin_level ASC", nativeQuery = true)
    List<AdministrativeDivision> findByCoordinates(@Param("latitude") double latitude, @Param("longitude") double longitude);
}
