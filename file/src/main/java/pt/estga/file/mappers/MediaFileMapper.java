package pt.estga.file.mappers;

import org.mapstruct.Mapper;
import pt.estga.file.dtos.MediaFileDto;
import pt.estga.file.entities.MediaFile;

@Mapper(componentModel = "spring")
public interface MediaFileMapper {
    MediaFileDto toDto(MediaFile entity);
}
