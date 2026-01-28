package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.shared.audit.AuditedEntity;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@Table(name = "mark_occurrence_proposal", indexes = {
        @Index(name = "idx_proposal_submitted_by", columnList = "submitted_by_id"),
        @Index(name = "idx_proposal_status", columnList = "status")
})
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

    @ManyToOne(fetch = FetchType.LAZY)
    private User submittedBy;

    private Instant submittedAt;

    @Builder.Default
    private boolean newMark = true;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

}
