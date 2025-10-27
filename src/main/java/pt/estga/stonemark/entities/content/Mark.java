package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.AuditableEntity;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mark extends ContentEntity {

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

    @Override
    public String getDisplayName() {
        return title;
    }
}
