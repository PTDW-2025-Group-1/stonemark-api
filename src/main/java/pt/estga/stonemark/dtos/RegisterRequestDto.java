package pt.estga.stonemark.dtos;

import lombok.*;
import pt.estga.stonemark.enums.Role;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterRequestDto {

    private String firstName;

    private String lastName;

    private String email;

    private String telephone;

    private String password;

    private Role role;

}
