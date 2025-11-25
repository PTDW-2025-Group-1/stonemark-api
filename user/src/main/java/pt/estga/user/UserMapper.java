package pt.estga.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pt.estga.user.dtos.ProfileUpdateRequestDto;
import pt.estga.user.dtos.UserDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);

    User registerRequestToEntity(RegisterRequestDto request);

    void update(@MappingTarget User user, ProfileUpdateRequestDto dto);

}
