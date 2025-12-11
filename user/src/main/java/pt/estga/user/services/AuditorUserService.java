package pt.estga.user.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuditorUserService {

    private final UserService userService;

    public Optional<User> findByUsername(String username) {
        return userService.findByUsername(username);
    }
}
