package pt.estga.stonemark.dtos.proposals;

import pt.estga.stonemark.dtos.file.MediaFileDto;

public record ProposedMarkDto(
        Long id,
        String name,
        String description,
        MediaFileDto mediaFile
) { }
