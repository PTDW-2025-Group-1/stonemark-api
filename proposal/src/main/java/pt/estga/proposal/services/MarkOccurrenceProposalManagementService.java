package pt.estga.proposal.services;

import pt.estga.proposal.entities.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalManagementService {

    MarkOccurrenceProposal approve(Long proposalId);

    MarkOccurrenceProposal reject(Long proposalId);

    MarkOccurrenceProposal pending(Long proposalId);

}
