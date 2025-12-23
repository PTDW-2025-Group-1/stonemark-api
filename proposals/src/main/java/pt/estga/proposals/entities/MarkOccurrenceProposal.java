package pt.estga.proposals.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.enums.SubmissionSource;
import pt.estga.shared.utils.DoubleListConverter;

import java.time.Instant;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrenceProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMark proposedMark;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMonument proposedMonument;

    @OneToOne(fetch = FetchType.LAZY)
    private MediaFile originalMediaFile;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

    private String userNotes;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    private Integer priority;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    @Builder.Default
    private boolean isSubmitted = false;

    @CreatedBy
    @Column(updatable = false)
    protected Long submittedById;

    @Column(updatable = false)
    private Instant submittedAt;

}
