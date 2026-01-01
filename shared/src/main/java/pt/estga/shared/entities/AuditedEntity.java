package pt.estga.shared.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pt.estga.shared.models.AuditActor;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditedEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "created_by_id")),
            @AttributeOverride(name = "type", column = @Column(name = "created_by_type")),
            @AttributeOverride(name = "identifier", column = @Column(name = "created_by_identifier"))
    })
    private AuditActor createdBy;

    @LastModifiedDate
    private Instant lastModifiedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "modified_by_id")),
            @AttributeOverride(name = "type", column = @Column(name = "modified_by_type")),
            @AttributeOverride(name = "identifier", column = @Column(name = "modified_by_identifier"))
    })
    private AuditActor modifiedBy;
}
