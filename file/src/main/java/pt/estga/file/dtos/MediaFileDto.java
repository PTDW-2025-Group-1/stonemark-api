package pt.estga.file.dtos;

public record MediaFileDto(
        Long id,
        String filename,
        String originalFilename,
        Long size
) { }
