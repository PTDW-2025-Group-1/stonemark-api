package pt.estga.file.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import pt.estga.file.enums.StorageProvider;
import pt.estga.user.entities.User;

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

    @CreatedDate
    private Instant uploadedAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    private User uploadedBy;

}
