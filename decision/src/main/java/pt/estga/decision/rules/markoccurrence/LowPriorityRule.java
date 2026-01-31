package pt.estga.decision.rules.markoccurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.rules.DecisionRule;
import pt.estga.decision.rules.DecisionRuleResult;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Component
@RequiredArgsConstructor
public class LowPriorityRule implements DecisionRule<MarkOccurrenceProposal> {

    private final ProposalDecisionProperties properties;

    @Override
    public DecisionRuleResult evaluate(MarkOccurrenceProposal proposal) {
        if (proposal.getPriority() != null && proposal.getPriority() < properties.getAutomaticRejectionThreshold()) {
            return DecisionRuleResult.conclusive(
                    DecisionOutcome.REJECT,
                    false, // Low priority rejections might not be "confident" enough to skip human oversight entirely, but for now we follow logic
                    "Priority " + proposal.getPriority() + " is below rejection threshold."
            );
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
