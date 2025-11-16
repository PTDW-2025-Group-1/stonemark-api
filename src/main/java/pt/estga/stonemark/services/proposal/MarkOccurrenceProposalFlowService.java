package pt.estga.stonemark.services.proposal;

import pt.estga.stonemark.dtos.proposal.ProposalStateDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMonumentRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMonumentRequestDto;

import java.io.IOException;

public interface MarkOccurrenceProposalFlowService {

    ProposalStateDto initiateProposal(byte[] photoData, String filename) throws IOException;

    ProposalStateDto handleExistingMonumentSelection(Long proposalId, SelectExistingMonumentRequestDto requestDto);

    ProposalStateDto handleNewMonumentProposal(Long proposalId, ProposeNewMonumentRequestDto requestDto);

    ProposalStateDto handleExistingMarkSelection(Long proposalId, SelectExistingMarkRequestDto requestDto);

    ProposalStateDto handleNewMarkProposal(Long proposalId, ProposeNewMarkRequestDto requestDto);

    ProposalStateDto submitProposal(Long proposalId);

    ProposalStateDto rejectProposal(Long proposalId);

}
