package pt.estga.chatbots.core.context;

import lombok.Data;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Data
public class ConversationContext {
    private String currentStateName;
    private Long domainUserId;
    // This will be moved to a feature-specific context later
    private MarkOccurrenceProposal proposal;
}
