package pt.estga.chatbots.core.shared.context;

import lombok.Data;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Data
public class ConversationContext {
    private ConversationState currentState;
    private Long domainUserId;
    // This will be moved to a feature-specific context later
    private MarkOccurrenceProposal proposal;
}
