package pt.estga.decision.dtos;

import pt.estga.proposal.enums.ProposalStatus;

import java.util.List;

public record ProposalFilter(
        List<ProposalStatus> statuses,
        Long submittedById,
        Long existingMonumentId,
        Long existingMarkId
) { }
