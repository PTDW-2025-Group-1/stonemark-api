package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticProposalDecisionMakerImpl implements AutomaticProposalDecisionMaker {

    private final MarkOccurrenceProposalManagementService managementService;

    @Override
    public void makeDecision(MarkOccurrenceProposal proposal) {
        log.info("Evaluating proposal {} for automatic decision. Priority: {}", proposal.getId(), proposal.getPriority());

        // Dummy logic for now
        // In the future, this could be based on priority thresholds, AI confidence scores, etc.
        
        // Example: If priority is very high (> 150), auto-approve
        if (proposal.getPriority() > 150) {
            log.info("Auto-approving proposal {} due to high priority.", proposal.getId());
            managementService.approve(proposal.getId());
        } 
        // Example: If priority is very low (< 10), auto-reject (maybe?)
        else if (proposal.getPriority() < 10) {
             // log.info("Auto-rejecting proposal {} due to low priority.", proposal.getId());
             // managementService.reject(proposal.getId());
             log.info("Proposal {} priority is low, but leaving for human review.", proposal.getId());
        }
        else {
            log.info("Proposal {} requires human review.", proposal.getId());
        }
    }
}
