package pt.estga.proposal.dtos;

public record MarkOccurrenceProposalStatsDto(
        long accepted,
        long underReview,
        long rejected
) {}
