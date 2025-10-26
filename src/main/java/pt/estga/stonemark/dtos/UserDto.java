package pt.estga.stonemark.dtos;

import lombok.*;
import pt.estga.stonemark.enums.Role;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String telephone;

    private Role role;

    private Instant createdAt;

}
