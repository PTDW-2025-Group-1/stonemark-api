package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.audit.AuditedEntity;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarkOccurrence extends AuditedEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mark mark;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monument monument;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile cover;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "embedding", columnDefinition = "vector(512)")
    @ColumnTransformer(write = "?::vector")
    private float[] embedding;

    private Long authorId;
    private String authorName;
    private Instant publishedAt;

    @Builder.Default
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        if (this.publishedAt == null) {
            this.publishedAt = Instant.now();
        }
    }

}
