package pt.estga.proposals.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.ProposalStatus;
import pt.estga.stonemark.enums.SubmissionSource;

import java.time.Instant;

@Entity
@Inheritance
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

}
