package pt.estga.stonemark.dtos.content;

import pt.estga.stonemark.dtos.file.MediaFileDto;
import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;

public record MarkDto(
        Long id,
        String title,
        MarkCategory category,
        MarkShape shape,
        MediaFileDto cover
) { }
