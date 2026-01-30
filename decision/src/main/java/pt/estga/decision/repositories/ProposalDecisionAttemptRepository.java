package pt.estga.decision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.decision.entities.ProposalDecisionAttempt;

import java.util.Optional;

public interface ProposalDecisionAttemptRepository extends JpaRepository<ProposalDecisionAttempt, Long> {
    Optional<ProposalDecisionAttempt> findFirstByProposalIdOrderByDecidedAtDesc(Long proposalId);
}
