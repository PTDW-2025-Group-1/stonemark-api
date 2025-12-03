package pt.estga.bots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.proposals.enums.ProposalStatus;

@Component
@RequiredArgsConstructor
public class AwaitingMonumentSelectionState implements ConversationState {

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MONUMENT_SELECTION;
    }
}
