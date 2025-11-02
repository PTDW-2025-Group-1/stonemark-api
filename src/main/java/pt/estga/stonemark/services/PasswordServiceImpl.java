package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.account.PasswordChangeRequestDto;
import pt.estga.stonemark.dtos.account.PasswordSetRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.PasswordResetRequest;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(User user, PasswordChangeRequestDto request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalStateException("Incorrect old password.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.update(user);
    }

    @Override
    public void setPassword(User user, PasswordSetRequestDto request) {
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.update(user);
    }

    @Override
    public void resetPassword(User user, PasswordResetRequest request) {
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.update(user);
    }
}
