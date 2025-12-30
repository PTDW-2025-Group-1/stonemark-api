package pt.estga.chatbot.context;

import lombok.Data;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.List;

@Data
public class ProposalContext {
    private Long proposalId;
    private List<String> suggestedMarkIds;
    private List<String> suggestedMonumentIds;

    public void clear() {
        this.proposalId = null;
        this.suggestedMarkIds = null;
        this.suggestedMonumentIds = null;
    }
}
