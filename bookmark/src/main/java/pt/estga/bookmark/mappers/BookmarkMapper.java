package pt.estga.bookmark.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.entities.Bookmark;

@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    @Mapping(target = "content", ignore = true)
    BookmarkDto toDto(Bookmark bookmark);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Bookmark toEntity(BookmarkDto dto);
}
