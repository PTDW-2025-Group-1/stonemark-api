package pt.estga.proposals.dtos;

import pt.estga.shared.dtos.file.MediaFileDto;

public record ProposedMarkDto(
        Long id,
        String name,
        String description,
        MediaFileDto mediaFile
) {
}
