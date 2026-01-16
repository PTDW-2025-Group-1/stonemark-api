package pt.estga.territory.entities;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;
import pt.estga.shared.audit.AuditedEntity;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AdministrativeDivision extends AuditedEntity {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private Long osmId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OsmType osmType;

    private Integer osmAdminLevel;

    private String boundary;

    private String name;

    private String namePt;

    @Column(columnDefinition = "geometry")
    private Geometry geometry;

    private Double areaKm2;

    @Enumerated(EnumType.STRING)
    private LogicalLevel logicalLevel;

    private Double logicalLevelConfidence;

    @Enumerated(EnumType.STRING)
    private ParentResolutionMethod parentResolutionMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    private AdministrativeDivision parent;

    private Double confidenceScore;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private Set<DivisionFlag> flags;

    private String countryCode;
}
