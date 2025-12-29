package pt.estga.proposal.services;

import pt.estga.proposal.entities.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalSubmissionService {

    MarkOccurrenceProposal submit(Long proposalId);

}
