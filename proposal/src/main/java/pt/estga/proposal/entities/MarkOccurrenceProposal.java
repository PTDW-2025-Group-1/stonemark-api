package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.shared.audit.AuditedEntity;
import pt.estga.shared.utils.DoubleListConverter;

import java.time.Instant;
import java.util.List;

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

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

    private String userNotes;
    private String monumentName;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    private Integer priority;
    
    private Integer credibilityScore;

    @Builder.Default
    private Boolean submitted = false;

    private Long submittedById;

    private Instant submittedAt;

    @Builder.Default
    private Boolean newMark = true;

    // ==== Decision state ====
    @OneToOne(fetch = FetchType.LAZY)
    private ProposalDecisionAttempt activeDecision;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

}
