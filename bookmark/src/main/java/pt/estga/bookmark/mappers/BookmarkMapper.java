package pt.estga.bookmark.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.entities.Bookmark;

@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    @Mapping(source = "targetType", target = "type")
    @Mapping(target = "content", ignore = true)
    BookmarkDto toDto(Bookmark bookmark);

    @Mapping(source = "type", target = "targetType")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Bookmark toEntity(BookmarkDto dto);
}

