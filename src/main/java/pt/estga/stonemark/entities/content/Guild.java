package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Guild implements Content {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private Date foundedDate;

    private Date dissolvedDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile defaultMark;

    @OneToMany
    private List<MediaFile> images;

    @Override
    public Content clone(Content content) {
        return null;
    }
}
