package pt.estga.chatbot.context;

import lombok.Data;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

@Data
public class ConversationContext {
    private ConversationState currentState;
    private Long domainUserId;
    private String userName;
    // This will be moved to a feature-specific context later
    private MarkOccurrenceProposal proposal;
    private List<String> suggestedMarkIds;
    private List<String> suggestedMonumentIds;
}
