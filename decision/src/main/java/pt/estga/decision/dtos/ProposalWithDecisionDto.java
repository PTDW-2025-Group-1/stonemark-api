package pt.estga.decision.dtos;

import pt.estga.proposal.dtos.ProposalWithRelationsDto;

public record ProposalWithDecisionDto(
        ProposalWithRelationsDto proposal,
        ActiveDecisionViewDto activeDecision
) {
}
