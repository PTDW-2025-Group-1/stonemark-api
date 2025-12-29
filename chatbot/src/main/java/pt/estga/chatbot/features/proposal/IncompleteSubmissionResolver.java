package pt.estga.chatbot.features.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.services.MarkOccurrenceProposalService;

@Component
@RequiredArgsConstructor
public class IncompleteSubmissionResolver {

    private final MarkOccurrenceProposalService service;

    public boolean hasIncompleteSubmission(Long userId) {
        return service.findIncompleteByUserId(userId).isPresent();
    }

    public MarkOccurrenceProposal getIncompleteSubmission(Long userId) {
        return service.findIncompleteByUserId(userId).orElse(null);
    }
}
