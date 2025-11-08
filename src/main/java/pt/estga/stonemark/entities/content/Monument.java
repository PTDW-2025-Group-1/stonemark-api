package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Monument extends AuditableEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private Double latitude;
    private Double longitude;

}
