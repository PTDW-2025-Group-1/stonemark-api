package pt.estga.bookmark.dtos;

import pt.estga.bookmark.enums.BookmarkTargetType;

public record BookmarkDto(
        Long id,
        BookmarkTargetType type,
        Long targetId,
        Object content
) {}

