package pt.estga.proposals.dtos;

import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.file.dtos.MediaFileDto;

public record MarkOccurrenceProposalDto(
        Long id,
        MediaFileDto originalMediaFile,
        MonumentResponseDto existingMonument,
        ProposedMonumentDto proposedMonument,
        MarkDto existingMark,
        ProposedMarkDto proposedMark,
        boolean isSubmitted
) {
}
