package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mark extends AuditableEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private MarkCategory category;

    @Enumerated(EnumType.STRING)
    private MarkShape shape;

    @OneToOne
    private MediaFile cover;

    @OneToMany
    private List<MediaFile> images;

}
