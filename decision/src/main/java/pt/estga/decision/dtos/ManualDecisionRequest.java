package pt.estga.decision.dtos;

import pt.estga.decision.enums.DecisionOutcome;

public record ManualDecisionRequest(
        DecisionOutcome outcome,
        String notes
) {}