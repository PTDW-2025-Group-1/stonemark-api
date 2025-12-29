package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.entities.ProposedMonument;

@Repository
public interface ProposedMonumentRepository extends JpaRepository<ProposedMonument, Long> {
}
