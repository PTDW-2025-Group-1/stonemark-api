package pt.estga.stonemark.entities.proposals;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.stonemark.enums.DecisionType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DecisionRecord extends AuditableProposalEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DecisionType decisionType;

    @ManyToOne
    private BaseProposal baseProposal;

    private String comments;

}
