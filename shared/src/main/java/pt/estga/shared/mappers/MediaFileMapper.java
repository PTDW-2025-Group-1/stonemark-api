package pt.estga.shared.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.file.MediaFileDto;
import pt.estga.stonemark.entities.MediaFile;

@Mapper(componentModel = "spring")
public interface MediaFileMapper {
    MediaFileDto toDto(MediaFile entity);
}
