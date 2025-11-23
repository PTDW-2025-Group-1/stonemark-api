package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProposedMonument {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;
}
