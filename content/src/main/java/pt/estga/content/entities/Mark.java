package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.utils.DoubleListConverter;

import java.util.List;

@NamedEntityGraph(
        name = "MyEntity.withMedia",
        attributeNodes = @NamedAttributeNode("cover")
)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mark extends AuditableContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String description;

    @OneToOne
    private MediaFile cover;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

}
