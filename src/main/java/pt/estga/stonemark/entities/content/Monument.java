package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Monument implements Content {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private Double latitude;

    private Double longitude;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

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
