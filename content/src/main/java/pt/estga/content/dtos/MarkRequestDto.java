package pt.estga.content.dtos;

public record MarkRequestDto(
        String description,
        float[] embedding,
        Long coverId,
        Boolean active
) { }
