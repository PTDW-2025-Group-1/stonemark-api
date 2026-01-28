package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;

import java.time.Instant;

public record MarkOccurrenceProposalListDto(
        Long id,
        String title,
        Long photoId,
        boolean submitted,
        ProposalStatus status,
        Instant submittedAt
) {}
