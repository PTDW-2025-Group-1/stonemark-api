package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
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
     * 6: District / province (distrito)
     * 8: Municipality / county (concelho)
     * 10: Parish / commune (freguesia)
     */
    private String adminLevel;
    private String borderType;

    @Column(columnDefinition = "TEXT")
    private String geometryJson;

}
