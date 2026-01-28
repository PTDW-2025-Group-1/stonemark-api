package pt.estga.decision.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.decision.entities.ProposalDecisionAttempt;

import java.util.List;

public interface ProposalDecisionAttemptRepository extends JpaRepository<ProposalDecisionAttempt, Long> {

    @EntityGraph(attributePaths = {"detectedMark", "detectedMonument", "decidedBy"})
    List<ProposalDecisionAttempt> findByProposalIdOrderByDecidedAtDesc(Long proposalId);

}
