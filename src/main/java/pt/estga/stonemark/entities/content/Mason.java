package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
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

    public Mason() {}

    public Mason(Long id, String firstName, String lastName, String fullName, String nickname, Date birthDate, Date deathDate, String biography, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.biography = biography;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Content clone(Content content) {
        return null;
    }
}
