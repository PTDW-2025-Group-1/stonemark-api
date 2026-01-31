package pt.estga.decision.rules;

import pt.estga.decision.enums.DecisionOutcome;

import java.util.Optional;

/**
 * The result of a single rule evaluation.
 */
public record DecisionRuleResult(
        DecisionOutcome outcome,
        boolean confident,
        String reason
) {
    public static DecisionRuleResult conclusive(DecisionOutcome outcome, boolean confident, String reason) {
        return new DecisionRuleResult(outcome, confident, reason);
    }

    public static Optional<DecisionRuleResult> empty() {
        return Optional.empty();
    }
}
