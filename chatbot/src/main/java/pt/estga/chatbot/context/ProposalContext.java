package pt.estga.chatbot.context;

import lombok.Data;
import pt.estga.proposal.entities.Proposal;

import java.util.List;

@Data
public class ProposalContext {
    private Proposal proposal;
    private List<String> suggestedMarkIds;
    private List<String> suggestedMonumentIds;

    public void clear() {
        this.proposal = null;
        this.suggestedMarkIds = null;
        this.suggestedMonumentIds = null;
    }
}
