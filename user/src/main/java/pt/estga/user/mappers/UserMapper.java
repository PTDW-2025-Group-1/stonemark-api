package pt.estga.user.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pt.estga.user.dtos.ProfileUpdateRequestDto;
import pt.estga.user.dtos.UserDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring", uses = UserContactMapper.class)
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);

    void update(@MappingTarget User user, ProfileUpdateRequestDto dto);

}
