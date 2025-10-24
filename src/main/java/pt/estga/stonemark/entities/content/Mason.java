package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
