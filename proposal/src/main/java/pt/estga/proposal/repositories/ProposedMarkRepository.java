package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.entities.ProposedMark;

@Repository
public interface ProposedMarkRepository extends JpaRepository<ProposedMark, Long> {
}
