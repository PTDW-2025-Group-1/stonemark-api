package pt.estga.stonemark.entities.content;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Mason {
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
}

