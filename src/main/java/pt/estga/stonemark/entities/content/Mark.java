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
public class Mark implements Content {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private MarkCategory category;

    @Enumerated(EnumType.STRING)
    private MarkShape shape;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    private MediaFile cover;

    @OneToMany
    private List<MediaFile> images;

    @Override
    public Content clone(Content content) {
        return Mark.builder()
            .title(this.title)
            .category(this.category)
            .shape(this.shape)
            .build();
    }
}
