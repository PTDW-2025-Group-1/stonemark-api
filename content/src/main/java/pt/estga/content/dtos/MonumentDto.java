package pt.estga.content.dtos;

public record MonumentDto(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude,
        String street,
        String houseNumber,
        AdministrativeDivisionDto parish,
        Long coverId
) { }
