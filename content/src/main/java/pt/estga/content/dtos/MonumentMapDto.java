package pt.estga.content.dtos;

public record MonumentMapDto(
        Long id,
        String name,
        String city,
        Double latitude,
        Double longitude,
        String protectionTitle,
        String website
) {
}
