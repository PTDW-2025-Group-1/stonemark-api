package pt.estga.stonemark.repositories.proposals;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {
}
