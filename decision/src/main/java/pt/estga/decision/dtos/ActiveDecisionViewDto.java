package pt.estga.decision.dtos;

import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;

import java.time.Instant;

public record ActiveDecisionViewDto(
        Long id,
        DecisionType type,
        DecisionOutcome outcome,
        String notes,
        Instant decidedAt,
        String decidedByUsername
) {
}
