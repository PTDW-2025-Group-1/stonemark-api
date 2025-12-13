package pt.estga.chatbots.telegram.state.factory;

import pt.estga.chatbots.telegram.state.ConversationState;
import pt.estga.proposals.enums.ProposalStatus;

public interface StateFactory {
    ConversationState createState(ProposalStatus status);
}
