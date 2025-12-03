package pt.estga.auth.services;

import jakarta.validation.Valid;
import pt.estga.user.entities.User;

public interface RegistrationService {

    void register(@Valid User user);

}
