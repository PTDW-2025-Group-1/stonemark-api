package pt.estga.user.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import pt.estga.file.entities.MediaFile;
import pt.estga.user.dtos.ProfileUpdateRequestDto;
import pt.estga.user.dtos.UserPublicDto;
import pt.estga.user.dtos.UserDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring", uses = UserContactMapper.class)
public interface UserMapper {

    @Mapping(target = "photoId", source = "photo.id")
    UserDto toDto(User user);

    @Mapping(target = "photoId", source = "photo.id")
    UserPublicDto toPublicDto(User user);

    @Mapping(source = "photoId", target = "photo", qualifiedByName = "idToMediaFile")
    User toEntity(UserDto dto);

    @Mapping(source = "photoId", target = "photo", qualifiedByName = "idToMediaFile")
    void update(@MappingTarget User user, ProfileUpdateRequestDto dto);

    @Named("idToMediaFile")
    default MediaFile idToMediaFile(Long id) {
        if (id == null) {
            return null;
        }
        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(id);
        return mediaFile;
    }
}
