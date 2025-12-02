package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalSubmissionService {

    MarkOccurrenceProposal submit(Long proposalId);

}
