package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.entities.MediaFile;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrence extends AuditableContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark mark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument monument;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile image;

}
