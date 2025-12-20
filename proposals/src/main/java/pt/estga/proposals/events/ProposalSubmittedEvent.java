package pt.estga.proposals.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Getter
public class ProposalSubmittedEvent extends ApplicationEvent {

    private final MarkOccurrenceProposal proposal;

    public ProposalSubmittedEvent(Object source, MarkOccurrenceProposal proposal) {
        super(source);
        this.proposal = proposal;
    }
}
