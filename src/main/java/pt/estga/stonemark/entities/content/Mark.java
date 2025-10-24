package pt.estga.stonemark.entities.content;

import jakarta.persistence.*;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;
import pt.estga.stonemark.interfaces.Content;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Mark implements Content {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private MarkCategory category;

    @Enumerated(EnumType.STRING)
    private MarkShape shape;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne
    private MediaFile cover;

    @OneToMany
    private List<MediaFile> images;

    public Mark() {}

    public Mark(Long id, String title, MarkCategory category, MarkShape shape) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.shape = shape;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MarkCategory getCategory() {
        return category;
    }

    public void setCategory(MarkCategory category) {
        this.category = category;
    }

    public MarkShape getShape() {
        return shape;
    }

    public void setShape(MarkShape shape) {
        this.shape = shape;
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

    @Override
    public Content clone(Content content) {
        return null;
    }
}
