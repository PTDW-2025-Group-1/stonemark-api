package pt.estga.stonemark.dtos.proposals;

import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.dtos.content.MonumentDto;
import pt.estga.stonemark.dtos.file.MediaFileDto;
import pt.estga.stonemark.enums.ProposalStatus;

public record MarkOccurrenceProposalDto(
        Long id,
        ProposalStatus status,
        MediaFileDto originalMediaFile,
        MarkDto existingMark,
        MonumentDto existingMonument,
        ProposedMarkDto proposedMark,
        ProposedMonumentDto proposedMonument
) { }
