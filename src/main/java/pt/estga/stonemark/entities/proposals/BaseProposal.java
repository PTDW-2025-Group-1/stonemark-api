package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.ProposalStatus;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseProposal {

    @Id
    @GeneratedValue
    private Long id;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User submittedBy;

    @CreatedDate
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewedBy;

    private Instant reviewedAt;

    @Column(length = 512)
    private String reviewComment;

}
