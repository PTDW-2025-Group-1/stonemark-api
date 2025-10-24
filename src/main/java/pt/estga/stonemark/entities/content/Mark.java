package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne
    private MediaFile cover;

    @OneToMany
    private List<MediaFile> images;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Content clone(Content content) {
        return Mark.builder()
            .title(this.title)
            .category(this.category)
            .shape(this.shape)
            .build();
    }
}
