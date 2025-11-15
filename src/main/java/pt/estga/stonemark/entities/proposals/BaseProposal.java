package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.ProposalStatus;
import pt.estga.stonemark.enums.SubmissionSource;

import java.time.Instant;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class BaseProposal extends AuditableProposalEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String userNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User submittedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    @OneToMany
    private List<DecisionRecord> decisionRecords;

}
