package pt.estga.stonemark.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequestDto {

    private String currentPassword;

    private String newPassword;

    private String confirmationPassword;

}