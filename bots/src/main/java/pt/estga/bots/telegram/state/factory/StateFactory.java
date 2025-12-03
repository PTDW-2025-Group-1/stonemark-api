package pt.estga.bots.telegram.state.factory;

import pt.estga.bots.telegram.state.ConversationState;
import pt.estga.proposals.enums.ProposalStatus;

public interface StateFactory {
    ConversationState createState(ProposalStatus status);
}
