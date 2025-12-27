package pt.estga.chatbot.features.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

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
