package pt.estga.stonemark.services;

import pt.estga.stonemark.dtos.account.PasswordChangeRequestDto;
import pt.estga.stonemark.dtos.account.PasswordSetRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.PasswordResetRequest;

public interface PasswordService {

    void setPassword(User user, PasswordSetRequestDto request);

    void changePassword(User user, PasswordChangeRequestDto request);

    void resetPassword(User user, PasswordResetRequest request);

}
