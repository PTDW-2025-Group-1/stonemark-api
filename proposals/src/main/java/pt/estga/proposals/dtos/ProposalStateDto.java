package pt.estga.proposals.dtos;

import pt.estga.proposals.enums.ProposalStatus;

public record ProposalStateDto(
        MarkOccurrenceProposalDto proposal,
        ProposalStatus status,
        String message
) { }
