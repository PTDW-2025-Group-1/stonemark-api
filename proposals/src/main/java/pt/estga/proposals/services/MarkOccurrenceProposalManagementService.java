package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalManagementService {
    MarkOccurrenceProposal approve(Long proposalId);
    MarkOccurrenceProposal reject(Long proposalId);
}
