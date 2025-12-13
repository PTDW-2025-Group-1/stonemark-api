package pt.estga.chatbots.telegram.context;

import lombok.Data;
import pt.estga.chatbots.telegram.state.ConversationState;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Data
public class ConversationContext {
    private long chatId;
    private ConversationState state;
    private Long proposalId;
    private Long userId;
    private MarkOccurrenceProposal proposal;

    public ConversationContext(long chatId) {
        this.chatId = chatId;
    }
}
