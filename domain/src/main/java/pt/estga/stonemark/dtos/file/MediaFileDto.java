package pt.estga.stonemark.dtos.file;

import java.time.Instant;

public record MediaFileDto(
        Long id,
        String fileName,
        String originalFileName,
        Long size,
        String storagePath,
        Instant uploadedAt
) { }
