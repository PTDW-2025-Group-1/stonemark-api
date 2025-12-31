package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProposalDecisionAttempt {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MarkOccurrenceProposal proposal;

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

    private Long decidedBy;

}
