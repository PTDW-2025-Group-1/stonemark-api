package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MarkOccurrence implements Content {

    @Id
    @GeneratedValue
    private Long id;

    private Long markId;

    private Long monumentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

    @OneToMany(mappedBy = "mark", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> images = new ArrayList<>();

    public MarkOccurrence() {}

    public MarkOccurrence(Long id, Long markId, Long monumentId) {
        this.id = id;
        this.markId = markId;
        this.monumentId = monumentId;
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

    public Long getMarkId() {
        return markId;
    }

    public void setMarkId(Long markId) {
        this.markId = markId;
    }

    public Long getMonumentId() {
        return monumentId;
    }

    public void setMonumentId(Long monumentId) {
        this.monumentId = monumentId;
    }

    public MediaFile getCover() {
        return cover;
    }

    public void setCover(MediaFile cover) {
        this.cover = cover;
    }

    public List<MediaFile> getImages() {
        return images;
    }

    public void setImages(List<MediaFile> images) {
        this.images = images;
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
