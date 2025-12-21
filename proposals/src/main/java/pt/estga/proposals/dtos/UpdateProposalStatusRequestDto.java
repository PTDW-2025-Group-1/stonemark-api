package pt.estga.proposals.dtos;

import pt.estga.proposals.enums.ProposalStatus;

public record UpdateProposalStatusRequestDto(
        ProposalStatus status
) {
}
