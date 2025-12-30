package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;

public record UpdateProposalStatusRequestDto(
        ProposalStatus status
) {
}
