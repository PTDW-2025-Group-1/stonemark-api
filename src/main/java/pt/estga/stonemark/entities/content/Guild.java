package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
public class Guild implements Content {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private Date foundedDate;

    private Date dissolvedDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile defaultMark;

    @OneToMany
    private List<MediaFile> images;

    public Guild() {}

    public Guild(Long id, String name, String description, Date foundedDate, Date dissolvedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.foundedDate = foundedDate;
        this.dissolvedDate = dissolvedDate;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(Date foundedDate) {
        this.foundedDate = foundedDate;
    }

    public Date getDissolvedDate() {
        return dissolvedDate;
    }

    public void setDissolvedDate(Date dissolvedDate) {
        this.dissolvedDate = dissolvedDate;
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

    public MediaFile getDefaultMark() {
        return defaultMark;
    }

    public void setDefaultMark(MediaFile defaultMark) {
        this.defaultMark = defaultMark;
    }

    public List<MediaFile> getImages() {
        return images;
    }

    public void setImages(List<MediaFile> images) {
        this.images = images;
    }

    @Override
    public Content clone(Content content) {
        return null;
    }
}
