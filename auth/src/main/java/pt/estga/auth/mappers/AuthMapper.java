package pt.estga.auth.mappers;

import org.mapstruct.Mapper;
import pt.estga.auth.dtos.RegisterRequestDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    User toUser(RegisterRequestDto request);

}
