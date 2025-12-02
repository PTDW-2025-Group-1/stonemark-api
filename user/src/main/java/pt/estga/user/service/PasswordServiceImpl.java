package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.user.repositories.UserRepository;
import pt.estga.user.dtos.PasswordChangeRequestDto;
import pt.estga.user.dtos.PasswordSetRequestDto;
import pt.estga.user.entities.User;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(User user, PasswordChangeRequestDto request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalStateException("Incorrect old password.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }

    @Override
    public void setPassword(User user, PasswordSetRequestDto request) {
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }
}
