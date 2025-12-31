package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.proposal.entities.ProposalDecisionAttempt;

public interface ProposalDecisionAttemptRepository extends JpaRepository<ProposalDecisionAttempt, Long> {
}
