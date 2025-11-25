package pt.estga.proposals.services;

import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;
import pt.estga.stonemark.dtos.proposal.SelectExistingMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMonumentRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMonumentRequestDto;

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
