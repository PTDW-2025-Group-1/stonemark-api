package pt.estga.user.service;

import pt.estga.user.dtos.EmailChangeRequestDto;
import pt.estga.user.entities.User;

public interface AccountManagementService {
    void requestEmailChange(User user, EmailChangeRequestDto request);
}
