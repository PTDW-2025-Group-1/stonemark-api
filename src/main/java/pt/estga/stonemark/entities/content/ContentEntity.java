package pt.estga.stonemark.entities.content;

import pt.estga.stonemark.entities.AuditableEntity;

public abstract class ContentEntity extends AuditableEntity {

    public abstract String getDisplayName();

}
