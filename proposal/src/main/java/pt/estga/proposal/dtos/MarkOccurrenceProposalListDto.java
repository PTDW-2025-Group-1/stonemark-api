package pt.estga.proposal.dtos;

import pt.estga.file.dtos.MediaFileDto;
import pt.estga.proposal.enums.ProposalStatus;

import java.time.Instant;

public record MarkOccurrenceProposalListDto(
        Long id,
        Long coverId,
        boolean isSubmitted,
        ProposalStatus status,
        Instant submittedAt
) {}
