package pt.estga.user.service;

import pt.estga.stonemark.dtos.account.PasswordChangeRequestDto;
import pt.estga.stonemark.dtos.account.PasswordSetRequestDto;
import pt.estga.stonemark.entities.User;

public interface PasswordService {

    void setPassword(User user, PasswordSetRequestDto request);

    void changePassword(User user, PasswordChangeRequestDto request);

}
