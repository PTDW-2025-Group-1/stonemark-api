package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.audit.AuditedEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Monument extends AuditedEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String externalId;

    private String name;
    private String protectionTitle;
    private String description;
    private Double latitude;
    private Double longitude;
    private String website;

    private String street;
    private String houseNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    private AdministrativeDivision parish;

    @ManyToOne(fetch = FetchType.LAZY)
    private AdministrativeDivision municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    private AdministrativeDivision district;

    @Builder.Default
    private Boolean active = true;

    @OneToOne
    private MediaFile cover;

}
