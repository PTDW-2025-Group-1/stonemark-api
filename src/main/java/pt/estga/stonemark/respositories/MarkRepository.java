package pt.estga.stonemark.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.Mark;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

}
