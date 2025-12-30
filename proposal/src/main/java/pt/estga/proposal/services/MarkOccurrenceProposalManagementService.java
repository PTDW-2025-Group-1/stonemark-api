package pt.estga.proposal.services;

public interface MarkOccurrenceProposalManagementService {

    void approve(Long proposalId);

    void reject(Long proposalId);

    void pending(Long proposalId);

}
