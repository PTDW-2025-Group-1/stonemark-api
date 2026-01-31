package pt.estga.decision.rules.markoccurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.rules.DecisionRule;
import pt.estga.decision.rules.DecisionRuleResult;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NewMonumentRule implements DecisionRule<MarkOccurrenceProposal> {

    private final ProposalDecisionProperties properties;

    @Override
    public DecisionRuleResult evaluate(MarkOccurrenceProposal proposal) {
        if (Boolean.TRUE.equals(properties.getRequireManualReviewForNewMonuments()) &&
                proposal.getExistingMonument() == null && proposal.getProposedMonument() != null) {
            
            return DecisionRuleResult.conclusive(
                    DecisionOutcome.INCONCLUSIVE,
                    true,
                    "Proposal requires new monument creation."
            );
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 10; // High priority check
    }
}
