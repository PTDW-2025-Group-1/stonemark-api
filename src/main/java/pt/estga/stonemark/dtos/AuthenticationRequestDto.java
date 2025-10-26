package pt.estga.stonemark.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthenticationRequestDto {

    private String email;

    private String password;

}
