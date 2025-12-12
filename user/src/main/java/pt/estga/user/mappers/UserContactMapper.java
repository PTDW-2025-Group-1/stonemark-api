package pt.estga.user.mappers;

import org.mapstruct.Mapper;
import pt.estga.user.dtos.UserContactDto;
import pt.estga.user.entities.UserContact;

@Mapper(componentModel = "spring")
public interface UserContactMapper {

    UserContactDto toDto(UserContact userContact);

    UserContact toEntity(UserContactDto userContactDto);
}

