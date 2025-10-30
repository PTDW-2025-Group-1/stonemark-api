package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.AuditableEntity;
import pt.estga.stonemark.entities.MediaFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrence extends ContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long markId;

    private Long monumentId;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

    @Override
    public String getDisplayName() {
        return "MarkOccurrence #" + id;
    }
}
