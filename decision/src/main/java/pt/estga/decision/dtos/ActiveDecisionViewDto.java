package pt.estga.decision.dtos;

import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;

import java.time.Instant;

public record ActiveDecisionViewDto(
        Long id,
        DecisionType type,
        DecisionOutcome outcome,
        Boolean confident,

        Long detectedMarkId,
        Long detectedMonumentId,

        String notes,
        Instant decidedAt,
        Long decidedBy
) {
}
