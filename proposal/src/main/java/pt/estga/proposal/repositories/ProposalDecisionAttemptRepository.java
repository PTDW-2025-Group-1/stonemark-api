package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.proposal.entities.ProposalDecisionAttempt;

import java.util.List;

public interface ProposalDecisionAttemptRepository extends JpaRepository<ProposalDecisionAttempt, Long> {

    List<ProposalDecisionAttempt> findByProposalIdOrderByDecidedAtDesc(Long proposalId);

}
