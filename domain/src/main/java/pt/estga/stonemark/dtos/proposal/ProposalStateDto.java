package pt.estga.stonemark.dtos.proposal;

import pt.estga.stonemark.dtos.proposals.MarkOccurrenceProposalDto;
import pt.estga.stonemark.enums.ProposalStatus;

public record ProposalStateDto(
        MarkOccurrenceProposalDto proposal,
        ProposalStatus nextAction,
        String message
) { }
