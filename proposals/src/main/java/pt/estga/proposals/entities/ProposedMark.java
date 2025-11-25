package pt.estga.proposals.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.*;
import pt.estga.file.entities.MediaFile;

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
