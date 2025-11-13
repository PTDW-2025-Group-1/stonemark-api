package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.entities.content.Monument;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MarkOccurrenceProposal extends BaseProposal {

    @OneToOne
    private MediaFile originalMediaFile;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    private MonumentData proposedMonumentData;

}
