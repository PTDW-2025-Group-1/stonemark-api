package pt.estga.shared.dtos.proposal;

import pt.estga.shared.dtos.proposals.MarkOccurrenceProposalDto;
import pt.estga.stonemark.enums.ProposalStatus;

public record ProposalStateDto(
        MarkOccurrenceProposalDto proposal,
        ProposalStatus nextAction,
        String message
) { }
