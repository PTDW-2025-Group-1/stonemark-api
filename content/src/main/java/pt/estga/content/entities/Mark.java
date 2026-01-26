package pt.estga.content.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.audit.AuditedEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mark extends AuditedEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String description;

    @OneToOne
    private MediaFile cover;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(columnDefinition = "vector")
    private double[] embedding;

    @Builder.Default
    private Boolean active = true;

}
