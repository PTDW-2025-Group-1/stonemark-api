package pt.estga.stonemark.dtos.proposal;

import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;
import pt.estga.stonemark.enums.ProposalStatus;

public record ProposalStateDto(
        MarkOccurrenceProposal proposal,
        ProposalStatus nextAction,
        String message
) { }
