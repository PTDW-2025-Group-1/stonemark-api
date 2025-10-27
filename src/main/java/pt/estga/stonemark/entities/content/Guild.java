package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.AuditableEntity;
import pt.estga.stonemark.entities.MediaFile;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Guild extends ContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private Date foundedDate;

    private Date dissolvedDate;

    @OneToOne(cascade = CascadeType.ALL)
    private Mark defaultMark;

    @OneToMany
    private List<MediaFile> images;

    @Override
    public String getDisplayName() {
        return name;
    }
}
