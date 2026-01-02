package pt.estga.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.AdministrativeDivision;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdministrativeDivisionRepository extends JpaRepository<AdministrativeDivision, Long> {
    Optional<AdministrativeDivision> findByName(String name);
    Optional<AdministrativeDivision> findByNameAndAdminLevel(String name, String adminLevel);
    List<AdministrativeDivision> findAllByNameIn(Collection<String> names);
}
