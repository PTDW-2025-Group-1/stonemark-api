package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.converters.DoubleListConverter;

import java.util.List;

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

    private String title;
    private String description;

    @OneToOne
    private MediaFile photo;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

}
