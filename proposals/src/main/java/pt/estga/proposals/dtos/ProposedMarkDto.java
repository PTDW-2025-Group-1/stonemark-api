package pt.estga.proposals.dtos;

import pt.estga.file.dtos.MediaFileDto;

public record ProposedMarkDto(
        Long id,
        String name,
        String description,
        MediaFileDto mediaFile
) {
}
