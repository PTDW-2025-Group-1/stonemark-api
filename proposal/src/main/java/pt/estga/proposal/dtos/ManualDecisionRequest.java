package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.DecisionOutcome;

public record ManualDecisionRequest(
        DecisionOutcome outcome,
        String notes
) {}