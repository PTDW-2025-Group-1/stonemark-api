package pt.estga.stonemark.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MediaFile {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column
    private String originalFileName;

    @Column(length = 100)
    private String contentType;

    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StorageProvider storageProvider;

    @Column(nullable = false, length = 1024)
    private String storagePath;

    @Column(length = 512)
    private String providerPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private boolean primaryImage;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public enum TargetType {
        MONUMENT, MARK, GUILD, MASON
    }

    public enum StorageProvider {
        LOCAL, AZURE
    }
}
