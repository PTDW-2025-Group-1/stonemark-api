package pt.estga.shared.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pt.estga.stonemark.dtos.account.ProfileUpdateRequestDto;
import pt.estga.stonemark.dtos.auth.RegisterRequestDto;
import pt.estga.stonemark.dtos.user.UserDto;
import pt.estga.stonemark.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);

    User registerRequestToEntity(RegisterRequestDto request);

    void update(@MappingTarget User user, ProfileUpdateRequestDto dto);

}
