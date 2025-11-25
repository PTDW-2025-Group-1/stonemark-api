package pt.estga.proposals.dtos;

import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.shared.dtos.content.MarkDto;
import pt.estga.shared.dtos.content.MonumentDto;
import pt.estga.shared.dtos.file.MediaFileDto;

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
