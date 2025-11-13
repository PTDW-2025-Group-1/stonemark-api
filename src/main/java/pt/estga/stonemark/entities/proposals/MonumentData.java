package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonumentData {
    private String name;
    private String description;
    private double latitude;
    private double longitude;
}
