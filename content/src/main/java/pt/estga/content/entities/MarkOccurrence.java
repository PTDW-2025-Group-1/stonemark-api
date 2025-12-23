package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.entities.FullAuditEntity;
import pt.estga.shared.utils.DoubleListConverter;
import pt.estga.user.entities.User;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrence extends FullAuditEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark mark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument monument;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    private User proposer;

}
