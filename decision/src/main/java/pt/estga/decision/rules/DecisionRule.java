package pt.estga.decision.rules;

import pt.estga.proposal.entities.Proposal;

/**
 * Represents a single rule for evaluating a proposal.
 *
 * @param <T> The type of proposal this rule applies to.
 */
public interface DecisionRule<T extends Proposal> {

    /**
     * Evaluates the proposal against this rule.
     *
     * @param proposal The proposal to evaluate.
     * @return A DecisionRuleResult containing the outcome if the rule matches, or empty if it doesn't.
     */
    DecisionRuleResult evaluate(T proposal);

    /**
     * Defines the order in which rules should be applied.
     * Lower values run first.
     *
     * @return The order value.
     */
    int getOrder();
}
