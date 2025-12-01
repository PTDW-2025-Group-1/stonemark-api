package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;

import java.util.List;

public interface ProposalSubmissionService {

    MarkOccurrenceProposal submit(Long proposalId);

}
