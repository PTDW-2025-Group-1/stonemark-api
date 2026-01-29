package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;

@Entity
@DiscriminatorValue("MARK_OCCURRENCE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MarkOccurrenceProposal extends Proposal {

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark existingMark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument existingMonument;

    @OneToOne(fetch = FetchType.LAZY)
    private MediaFile originalMediaFile;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "embedding", columnDefinition = "vector(512)")
    @ColumnTransformer(write = "?::vector")
    private float[] embedding;

    private String monumentName;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private boolean newMark = true;

}
