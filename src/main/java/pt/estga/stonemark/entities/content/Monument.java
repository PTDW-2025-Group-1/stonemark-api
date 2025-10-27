package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.AuditableEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Monument extends ContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private Double latitude;
    private Double longitude;

    @Override
    public String getDisplayName() {
        return name;
    }
}
