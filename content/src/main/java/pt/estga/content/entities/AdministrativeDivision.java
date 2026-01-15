package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;
import pt.estga.shared.audit.AuditedEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdministrativeDivision extends AuditedEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    /**
     * Administrative level of the division.
     * 6: District (distrito)
     * 7: Municipality (concelho)
     * 8: Parish (freguesia)
     */
    private int adminLevel;

    @Column(columnDefinition = "geometry")
    private Geometry geometry;

}
