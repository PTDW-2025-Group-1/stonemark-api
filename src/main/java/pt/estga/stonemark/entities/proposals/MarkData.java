package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkData {
    private String name;
    private String description;
}
