package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import pt.estga.stonemark.entities.MediaFile;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProposedMark {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;

    @OneToOne
    private MediaFile mediaFile;

}
