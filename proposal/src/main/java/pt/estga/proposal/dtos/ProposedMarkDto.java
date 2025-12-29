package pt.estga.proposal.dtos;

import pt.estga.file.dtos.MediaFileDto;

public record ProposedMarkDto(
        Long id,
        String description,
        MediaFileDto mediaFile
) {
}
