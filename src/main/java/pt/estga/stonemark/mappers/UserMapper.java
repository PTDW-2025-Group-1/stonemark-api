package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.auth.RegisterRequestDto;
import pt.estga.stonemark.dtos.user.UserDto;
import pt.estga.stonemark.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User registerRequestToUser(RegisterRequestDto request);

}
