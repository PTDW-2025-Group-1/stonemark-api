package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Mark;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

    @EntityGraph(value = "MyEntity.withMedia")
    @Query("SELECT m FROM Mark m")
    Page<Mark> findAllWithCover(Pageable pageable);

    @EntityGraph(value = "MyEntity.withMedia")
    Optional<Mark> findWithCoverById(Long id);

    @Query(value = "SELECT id, 1 - (CAST(embedding AS vector) <=> CAST(:vector AS vector)) as similarity " +
            "FROM mark " +
            "WHERE embedding IS NOT NULL " +
            "ORDER BY similarity DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findSimilarMarks(@Param("vector") String vector);

}
