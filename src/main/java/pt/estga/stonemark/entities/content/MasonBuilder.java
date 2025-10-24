package pt.estga.stonemark.entities.content;

import java.time.LocalDateTime;
import java.util.Date;

public class MasonBuilder {
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

    public MasonBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public MasonBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public MasonBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public MasonBuilder setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public MasonBuilder setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public MasonBuilder setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public MasonBuilder setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
        return this;
    }

    public MasonBuilder setBiography(String biography) {
        this.biography = biography;
        return this;
    }

    public MasonBuilder setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MasonBuilder setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Mason createMason() {
        return new Mason(id, firstName, lastName, fullName, nickname, birthDate, deathDate, biography, createdAt, updatedAt);
    }
}