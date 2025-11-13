package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.ProposalStatus;
import pt.estga.stonemark.enums.SubmissionSource;

import java.time.Instant;

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
    private ProposalStatus status = ProposalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User submittedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

}
