package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.ProposalStatus;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseProposal {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User submittedBy;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 512)
    private String reviewComment;

}
