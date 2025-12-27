package pt.estga.content.dtos;

public record MonumentListDto(
        Long id,
        Long coverId,
        String name,
        String city
) {
}
