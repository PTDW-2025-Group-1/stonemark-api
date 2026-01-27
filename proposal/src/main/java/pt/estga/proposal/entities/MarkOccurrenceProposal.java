package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.shared.audit.AuditedEntity;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrenceProposal extends AuditedEntity {

    @Id
    @GeneratedValue
    private Long id;

    // ==== User submission snapshot ====
    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @OneToOne(fetch = FetchType.LAZY)
    private MediaFile originalMediaFile;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(columnDefinition = "vector")
    private double[] embedding;

    private String userNotes;
    private String monumentName;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    private Integer priority;
    
    private Integer credibilityScore;

    @Builder.Default
    private boolean submitted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private User submittedBy;

    private Instant submittedAt;

    @Builder.Default
    private boolean newMark = true;

    // ==== Decision state ====
    @OneToOne(fetch = FetchType.LAZY)
    private ProposalDecisionAttempt activeDecision;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    public void applyDecision(ProposalDecisionAttempt decision) {
        this.activeDecision = decision;
        
        if (decision.getType() == DecisionType.MANUAL) {
            this.status = decision.getOutcome() == DecisionOutcome.ACCEPT
                    ? ProposalStatus.MANUALLY_ACCEPTED
                    : ProposalStatus.MANUALLY_REJECTED;
        } else {
            // Automatic
            if (decision.getOutcome() == DecisionOutcome.ACCEPT) {
                this.status = ProposalStatus.AUTO_ACCEPTED;
            } else if (decision.getOutcome() == DecisionOutcome.REJECT) {
                this.status = ProposalStatus.AUTO_REJECTED;
            } else {
                // Inconclusive
                if (this.existingMonument == null && this.monumentName != null) {
                    this.status = ProposalStatus.PENDING_MONUMENT_CREATION;
                } else {
                    this.status = ProposalStatus.UNDER_REVIEW;
                }
            }
        }
    }

}
