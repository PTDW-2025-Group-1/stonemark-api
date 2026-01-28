package pt.estga.decision.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProposalDecisionAttempt {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DecisionType type;

    @Enumerated(EnumType.STRING)
    private DecisionOutcome outcome;

    private Boolean confident;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark detectedMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument detectedMonument;

    private String notes;

    private Instant decidedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MarkOccurrenceProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    private User decidedBy;

}
