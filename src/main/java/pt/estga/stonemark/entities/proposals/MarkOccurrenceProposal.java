package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.enums.ProposalStatus;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MarkOccurrenceProposal extends BaseProposal {

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    @OneToOne
    private MediaFile originalMediaFile;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "proposed_mark_name")),
            @AttributeOverride(name = "description", column = @Column(name = "proposed_mark_description"))
    })
    private MarkData proposedMarkData;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "proposed_monument_name")),
            @AttributeOverride(name = "description", column = @Column(name = "proposed_monument_description")),
            @AttributeOverride(name = "latitude", column = @Column(name = "proposed_monument_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "proposed_monument_longitude"))
    })
    private MonumentData proposedMonumentData;

}
