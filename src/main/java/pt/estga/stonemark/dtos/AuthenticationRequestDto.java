package pt.estga.stonemark.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequestDto {

    private String email;

    private String password;

}
