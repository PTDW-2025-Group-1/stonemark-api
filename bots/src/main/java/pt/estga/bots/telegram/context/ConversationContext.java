package pt.estga.bots.telegram.context;

import lombok.Data;
import pt.estga.bots.telegram.state.ConversationState;

@Data
public class ConversationContext {
    private long chatId;
    private ConversationState state;
    private Long proposalId;

    public ConversationContext(long chatId) {
        this.chatId = chatId;
    }
}
