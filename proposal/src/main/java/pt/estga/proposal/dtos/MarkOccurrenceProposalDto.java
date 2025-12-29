package pt.estga.proposal.dtos;

import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.file.dtos.MediaFileDto;

public record MarkOccurrenceProposalDto(
        Long id,
        Integer priority,
        MediaFileDto originalMediaFile,
        MonumentResponseDto existingMonument,
        ProposedMonumentDto proposedMonument,
        MarkDto existingMark,
        boolean newMark,
        String userNotes,
        boolean isSubmitted
) {
}
