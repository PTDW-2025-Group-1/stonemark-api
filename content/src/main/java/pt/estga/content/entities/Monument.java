package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
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

    private String name;
    private String protectionTitle;
    private String description;
    private Double latitude;
    private Double longitude;
    private String website;
    private String address;

    @Builder.Default
    private Boolean active = true;

    @OneToOne
    private MediaFile cover;

}
