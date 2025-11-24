package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.MediaFile;

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

    @OneToOne
    private MediaFile vector;

}
