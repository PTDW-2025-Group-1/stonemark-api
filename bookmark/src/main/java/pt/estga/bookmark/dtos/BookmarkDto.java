package pt.estga.bookmark.dtos;

import pt.estga.file.enums.TargetType;

public record BookmarkDto(
        Long id,
        TargetType type,
        Long targetId,
        Object content
) {}

