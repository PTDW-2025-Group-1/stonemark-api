package pt.estga.file.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import pt.estga.file.enums.MediaStatus;
import pt.estga.file.enums.StorageProvider;

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
    private String filename;

    @Column
    private String originalFilename;

    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StorageProvider storageProvider;

    @Column(nullable = false, length = 1024)
    private String storagePath;

    @Column(length = 512)
    private String providerPublicId;

    @CreatedDate
    private Instant uploadedAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MediaStatus status;

}
