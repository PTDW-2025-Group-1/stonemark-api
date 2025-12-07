package pt.estga.content.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Mark;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {
    Page<Mark> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
