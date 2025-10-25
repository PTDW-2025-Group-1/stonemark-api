package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mason implements Content {

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Content clone(Content content) {
        return Mason.builder()
            .firstName(this.firstName)
            .lastName(this.lastName)
            .fullName(this.fullName)
            .nickname(this.nickname)
            .birthDate(this.birthDate)
            .deathDate(this.deathDate)
            .biography(this.biography)
            .build();
    }
}
