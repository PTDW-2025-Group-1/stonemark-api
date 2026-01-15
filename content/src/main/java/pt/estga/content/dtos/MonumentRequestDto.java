package pt.estga.content.dtos;

public record MonumentRequestDto(
        String name,
        String description,
        String protectionTitle,
        String website,
        Double latitude,
        Double longitude,
        String street,
        String houseNumber,
        Long parishId
) { }
