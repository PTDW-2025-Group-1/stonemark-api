package pt.estga.decision.dtos;

import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;

import java.time.Instant;

public record DecisionHistoryItem(
        Long id,
        DecisionType type,
        DecisionOutcome outcome,
        Boolean confident,
        Instant decidedAt,
        Long decidedBy,
        String notes
) { }
