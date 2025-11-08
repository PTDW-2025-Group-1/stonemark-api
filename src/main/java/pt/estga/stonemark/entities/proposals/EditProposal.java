package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import pt.estga.stonemark.entities.content.Guild;

@Entity
public class EditProposal extends BaseProposal {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Guild targetGuild;

    @Lob
    private String proposedChangesJson;

}
