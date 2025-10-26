package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrence extends AuditableEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long markId;

    private Long monumentId;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

    @OneToMany(mappedBy = "mark", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> images = new ArrayList<>();

}
