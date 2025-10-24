package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Monument implements Content {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private Double latitude;

    private Double longitude;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
        return Monument.builder()
            .name(this.name)
            .description(this.description)
            .latitude(this.latitude)
            .longitude(this.longitude)
            .build();
    }
}
