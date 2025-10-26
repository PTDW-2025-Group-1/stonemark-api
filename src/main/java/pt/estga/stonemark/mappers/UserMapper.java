package pt.estga.stonemark.mappers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.dtos.UserDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;

import java.time.ZoneOffset;

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
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().atZone(ZoneOffset.UTC).toInstant()
                        : null)
                .build();
    }

    public static User registerRequestToUser(RegisterRequestDto request, PasswordEncoder passwordEncoder) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();
    }
}
