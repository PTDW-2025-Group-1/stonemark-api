package pt.estga.stonemark.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChangePasswordRequestDto {

    private String currentPassword;

    private String newPassword;

    private String confirmationPassword;

}