package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.ChangePasswordRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.exceptions.PasswordMismatchException;
import pt.estga.stonemark.exceptions.UserNotFoundException;
import pt.estga.stonemark.repositories.UserRepository;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceHibernateImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Override
    public void processPasswordChangeRequest(ChangePasswordRequestDto request, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }
        if (!request.newPassword().equals(request.confirmationPassword())) {
            throw new PasswordMismatchException("Passwords are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
