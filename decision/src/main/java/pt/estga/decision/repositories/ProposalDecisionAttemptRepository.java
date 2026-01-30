package pt.estga.decision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.decision.entities.ProposalDecisionAttempt;

public interface ProposalDecisionAttemptRepository extends JpaRepository<ProposalDecisionAttempt, Long> {

}
