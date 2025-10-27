package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.dtos.UserDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;

import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User registerRequestToUser(RegisterRequestDto request);

}
