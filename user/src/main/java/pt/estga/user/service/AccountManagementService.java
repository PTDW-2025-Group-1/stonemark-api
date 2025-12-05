package pt.estga.user.service;

import pt.estga.user.dtos.EmailChangeRequestDto;
import pt.estga.user.dtos.TelephoneChangeRequestDto;
import pt.estga.user.entities.User;

public interface AccountManagementService {

    void requestEmailChange(User user, EmailChangeRequestDto request);

    void requestTelephoneChange(User user, TelephoneChangeRequestDto newTelephone);

}
