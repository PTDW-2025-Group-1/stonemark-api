package pt.estga.stonemark.mappers;

import org.springframework.stereotype.Component;
import pt.estga.stonemark.dtos.UserDto;
import pt.estga.stonemark.entities.User;

@Component
public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .telephone(userDto.getTelephone())
                .role(userDto.getRole())
                .build();
    }
}
