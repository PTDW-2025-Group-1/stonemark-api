package pt.estga.file.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.file.enums.MediaVariantType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"media_file_id", "type"}
    )
)
public class MediaVariant {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private MediaFile mediaFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaVariantType type;

    @Column(nullable = false, length = 1024)
    private String storagePath;

    private Integer width;
    private Integer height;
    private Long size;
}
