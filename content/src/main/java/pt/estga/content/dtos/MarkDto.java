package pt.estga.content.dtos;

import java.util.List;

public record MarkDto(
        Long id,
        String title,
        String description,
        List<Double> embedding,
        Long coverId,
        Boolean active
) { }
