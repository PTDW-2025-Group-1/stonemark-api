package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;

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
