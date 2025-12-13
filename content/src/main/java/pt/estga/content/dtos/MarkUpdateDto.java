package pt.estga.content.dtos;

import java.util.List;

public record MarkUpdateDto(
        String title,
        String description,
        List<Double> embedding,
        Long coverId
) { }
