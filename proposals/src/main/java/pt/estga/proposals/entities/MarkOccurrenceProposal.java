package pt.estga.proposals.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MarkOccurrenceProposal extends BaseProposal {

    @OneToOne(fetch = FetchType.LAZY)
    private MediaFile originalMediaFile;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMark proposedMark;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProposedMonument proposedMonument;

}
