package pt.estga.proposals.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.enums.SubmissionSource;
import pt.estga.shared.converters.DoubleListConverter;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MarkOccurrenceProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private MediaFile originalMediaFile;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMark proposedMark;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMonument proposedMonument;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String suggestedMarkIds;

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
