package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkOccurrenceRepository extends JpaRepository<MarkOccurrence, Long> {

    Page<MarkOccurrence> findAllByMarkId(Long markId, Pageable pageable);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    @Query("SELECT DISTINCT mo.mark FROM MarkOccurrence mo WHERE mo.monument.id = :monumentId")
    List<Mark> findDistinctMarksByMonumentId(@Param("monumentId") Long monumentId);

    @Query("SELECT DISTINCT mo.monument FROM MarkOccurrence mo WHERE mo.mark.id = :markId")
    List<Monument> findDistinctMonumentsByMarkId(@Param("markId") Long markId);

    Page<MarkOccurrence> findAllByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);

    @EntityGraph(attributePaths = {"monument", "proposer"})
    @Query("SELECT m FROM MarkOccurrence m WHERE m.id = :id")
    Optional<MarkOccurrence> findByIdWithRelationships(@Param("id") Long id);
}
