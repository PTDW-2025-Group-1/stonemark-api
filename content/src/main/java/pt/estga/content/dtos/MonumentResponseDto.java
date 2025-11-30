package pt.estga.content.dtos;

public record MonumentResponseDto(
        Long id,
        String name,
        String description,
        String protectionTitle,
        String website,
        Double latitude,
        Double longitude,
        String address,
        String city
) {
}
