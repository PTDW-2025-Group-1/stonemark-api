package pt.estga.file.dtos;

import pt.estga.file.enums.MediaStatus;

public record MediaFileDto(
        Long id,
        String filename,
        String originalFilename,
        Long size,
        MediaStatus status
) { }
