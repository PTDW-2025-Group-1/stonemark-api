package pt.estga.proposals.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {
}
