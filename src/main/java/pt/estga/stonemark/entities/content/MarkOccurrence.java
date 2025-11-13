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
public class MarkOccurrence extends AuditableContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long markId;

    private Long monumentId;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

}
