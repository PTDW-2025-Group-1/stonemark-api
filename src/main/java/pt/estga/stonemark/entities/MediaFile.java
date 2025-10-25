package pt.estga.stonemark.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.entities.content.MarkOccurrence;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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

    @ManyToOne
    @JoinColumn(name = "mark_id")
    private MarkOccurrence mark;

    @PrePersist
    void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
