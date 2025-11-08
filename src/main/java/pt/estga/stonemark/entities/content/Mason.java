package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mason extends AuditableEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String fullName;
    private String nickname;
    private Date birthDate;
    private Date deathDate;
    private String biography;

}
