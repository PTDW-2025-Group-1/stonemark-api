package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

@Entity
public class CreateProposal extends BaseProposal {

    @Lob
    private String proposedEntityJson;

}
