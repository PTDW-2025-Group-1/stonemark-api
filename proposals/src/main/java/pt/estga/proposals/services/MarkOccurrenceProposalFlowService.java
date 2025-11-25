package pt.estga.proposals.services;

import pt.estga.proposals.dtos.ProposeNewMarkRequestDto;
import pt.estga.proposals.dtos.ProposeNewMonumentRequestDto;
import pt.estga.proposals.dtos.SelectExistingMarkRequestDto;
import pt.estga.proposals.dtos.SelectExistingMonumentRequestDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.io.IOException;

public interface MarkOccurrenceProposalFlowService {

    MarkOccurrenceProposal initiateProposal(byte[] photoData, String filename) throws IOException;

    MarkOccurrenceProposal handleExistingMonumentSelection(Long proposalId, SelectExistingMonumentRequestDto requestDto);

    MarkOccurrenceProposal handleNewMonumentProposal(Long proposalId, ProposeNewMonumentRequestDto requestDto);

    MarkOccurrenceProposal handleExistingMarkSelection(Long proposalId, SelectExistingMarkRequestDto requestDto);

    MarkOccurrenceProposal handleNewMarkProposal(Long proposalId, ProposeNewMarkRequestDto requestDto);

    MarkOccurrenceProposal submitProposal(Long proposalId);

    MarkOccurrenceProposal approveProposal(Long proposalId);

    MarkOccurrenceProposal rejectProposal(Long proposalId);
}
