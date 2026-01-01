package pt.estga.shared.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import pt.estga.shared.models.AuditActor;

@Entity
@Getter
@Setter
@RevisionEntity(AppRevisionListener.class)
public class AuditRevisionEntity extends DefaultRevisionEntity {

    @Embedded
    private AuditActor actor;

}
