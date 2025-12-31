package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;

import java.time.Instant;

public record DecisionHistoryItem(
        Long id,
        DecisionType type,
        DecisionOutcome outcome,
        Boolean confident,
        Instant decidedAt,
        Long decidedBy,
        String notes
) {
}
