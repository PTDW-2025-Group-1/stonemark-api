package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.events.ProposalSubmittedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkOccurrenceProposalSubmissionServiceImpl implements MarkOccurrenceProposalSubmissionService {

    private final MarkOccurrenceProposalService proposalService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MarkOccurrenceProposal submit(Long proposalId) {
        log.info("Submitting proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = proposalService.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during submission", proposalId);
                    return new RuntimeException("Proposal not found");
                });

        proposal.setSubmitted(true);

        // Ensure embedding is propagated to ProposedMark if it exists and hasn't been set yet
        ProposedMark proposedMark = proposal.getProposedMark();
        if (proposedMark != null && proposal.getEmbedding() != null && (proposedMark.getEmbedding() == null || proposedMark.getEmbedding().isEmpty())) {
            log.info("Propagating embedding to ProposedMark for proposal ID: {}", proposalId);
            proposedMark.setEmbedding(proposal.getEmbedding());
        }

        MarkOccurrenceProposal updatedProposal = proposalService.update(proposal);
        log.info("Proposal with ID: {} submitted successfully", proposalId);
        
        eventPublisher.publishEvent(new ProposalSubmittedEvent(this, updatedProposal));
        log.debug("Published ProposalSubmittedEvent for proposal ID: {}", proposalId);

        return updatedProposal;
    }
}
