package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.user.enums.ServiceRole;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServiceAccount {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private Boolean enabled;

    @Column(nullable = false)
    private String secretHash; // for JWT client credentials

    @Enumerated(EnumType.STRING)
    private ServiceRole role;

}
