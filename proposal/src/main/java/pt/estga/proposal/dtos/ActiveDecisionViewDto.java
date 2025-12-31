package pt.estga.proposal.dtos;

import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;

import java.time.Instant;

public record ActiveDecisionViewDto(
        Long id,
        DecisionType type,
        DecisionOutcome outcome,
        Boolean confident,

        Mark detectedMark,
        Monument detectedMonument,

        String notes,
        Instant decidedAt,
        Long decidedBy
) {
}
