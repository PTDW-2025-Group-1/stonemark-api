package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Monument extends AuditableContentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String website;
    private String protectionTitle;
    private String address;
    private String city;

}
