package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;

import java.util.List;

@Repository
public interface MarkOccurrenceRepository extends JpaRepository<MarkOccurrence, Long> {

    Page<MarkOccurrence> findAllByMarkId(Long markId, Pageable pageable);

    Page<MarkOccurrence> findByMonumentId(Long monumentId, Pageable pageable);

    @Query("SELECT DISTINCT m FROM MarkOccurrence mo JOIN mo.mark m ORDER BY m.title")
    List<Mark> findDistinctMarksWithOccurrences();

    @Query("SELECT DISTINCT mo.monument FROM MarkOccurrence mo WHERE mo.mark.id = :markId")
    List<Monument> findDistinctMonumentsByMarkId(@Param("markId") Long markId);

    Page<MarkOccurrence> findAllByMarkIdAndMonumentId(Long markId, Long monumentId, Pageable pageable);

    long countByMonumentId(Long monumentId);

    long countByMarkId(Long markId);
}
