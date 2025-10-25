package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.ChangePasswordRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.repositories.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceHibernateImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean updateRole(Long userId, Role newRole) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
            user.setRole(newRole);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean changePassword(ChangePasswordRequestDto request, Principal connectedUser) {
        try {
            var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

            // Check if the current password is correct
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalStateException("Wrong password");
            }

            // Check if the two new passwords are the same
            if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
                throw new IllegalStateException("Passwords are not the same");
            }

            // Update the password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
