package pt.estga.proposals.dtos;

import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MonumentDto;
import pt.estga.file.dtos.MediaFileDto;
import pt.estga.proposals.enums.ProposalStatus;

public record MarkOccurrenceProposalDto(
        Long id,
        ProposalStatus status,
        MediaFileDto originalMediaFile,
        MonumentDto existingMonument,
        ProposedMonumentDto proposedMonument,
        MarkDto existingMark,
        ProposedMarkDto proposedMark
) {
}
