package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;

import java.time.Instant;

public record MarkOccurrenceProposalListDto(
        Long id,
        Long photoId,
        boolean submitted,
        ProposalStatus status,
        Instant submittedAt
) {}
