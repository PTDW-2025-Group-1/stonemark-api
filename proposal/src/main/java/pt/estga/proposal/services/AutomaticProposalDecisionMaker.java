package pt.estga.proposal.services;

import pt.estga.proposal.entities.MarkOccurrenceProposal;

public interface AutomaticProposalDecisionMaker {
    void makeDecision(MarkOccurrenceProposal proposal);
}
