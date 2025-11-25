package pt.estga.user.service;

import pt.estga.user.dtos.PasswordChangeRequestDto;
import pt.estga.user.dtos.PasswordSetRequestDto;
import pt.estga.user.entities.User;

public interface PasswordService {

    void setPassword(User user, PasswordSetRequestDto request);

    void changePassword(User user, PasswordChangeRequestDto request);

}
