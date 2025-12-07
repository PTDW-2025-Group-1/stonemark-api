package pt.estga.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.user.entities.User;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuditorUserService {

    private final UserService userService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }
}
