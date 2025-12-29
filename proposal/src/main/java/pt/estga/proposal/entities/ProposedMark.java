package pt.estga.proposal.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.utils.DoubleListConverter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProposedMark {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;

    @OneToOne
    private MediaFile mediaFile;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

}
