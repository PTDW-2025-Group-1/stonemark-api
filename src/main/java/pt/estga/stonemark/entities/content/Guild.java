package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile defaultMark;

    @OneToMany
    private List<MediaFile> images;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Content clone(Content content) {
        return null;
    }
}
