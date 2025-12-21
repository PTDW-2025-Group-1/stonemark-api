package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

public interface AutomaticProposalDecisionMaker {
    void makeDecision(MarkOccurrenceProposal proposal);
}
