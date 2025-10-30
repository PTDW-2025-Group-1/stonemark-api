package pt.estga.stonemark.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pt.estga.stonemark.entities.content.MarkOccurrence;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;

import java.time.Instant;

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

    @CreationTimestamp
    private Instant uploadedAt;

}
