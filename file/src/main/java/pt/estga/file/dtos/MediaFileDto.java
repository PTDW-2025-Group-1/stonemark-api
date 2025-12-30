package pt.estga.file.dtos;

import java.time.Instant;

public record MediaFileDto(
        Long id,
        String filename,
        String originalFilename,
        Long size,
        String storagePath,
        Instant uploadedAt
) { }
