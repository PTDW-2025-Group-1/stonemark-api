package pt.estga.stonemark.dtos;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.stonemark.entities.User;
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

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
            .firstName(this.firstName)
            .lastName(this.lastName)
            .telephone(this.telephone)
            .email(this.email)
            .password(passwordEncoder.encode(this.password))
            .role(this.role != null && this.role != Role.USER ? this.role : Role.USER)
            .build();
    }

}
