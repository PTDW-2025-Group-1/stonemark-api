package pt.estga.stonemark.services.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.config.JwtService;
import pt.estga.stonemark.dtos.auth.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.auth.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.auth.RegisterRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.EmailVerificationRequiredException;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.mappers.UserMapper;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.token.AccessTokenService;
import pt.estga.stonemark.services.token.RefreshTokenService;
import pt.estga.stonemark.services.token.VerificationTokenService;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;
    private final VerificationService verificationService;
    private final VerificationTokenService verificationTokenService;

    @Value("${application.security.email-verification-required:true}")
    private boolean emailVerificationRequired;

    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> register(RegisterRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        var email = request.email();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be null or blank");
        }
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("email already in use");
        }

        User parsedUser = mapper.registerRequestToUser(request);
        parsedUser.setPassword(passwordEncoder.encode(request.password()));
        if (parsedUser.getRole() == null) {
            parsedUser.setRole(Role.USER);
        }

        parsedUser.setEnabled(!emailVerificationRequired);

        User user = userService.create(parsedUser);

        if (emailVerificationRequired) {
            verificationService.createAndSendToken(user, VerificationTokenPurpose.EMAIL_VERIFICATION);
            throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
        } else {
            return generateAuthenticationResponse(user);
        }
    }

    @NotNull
    private Optional<AuthenticationResponseDto> generateAuthenticationResponse(User user) {
        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return Optional.of(new AuthenticationResponseDto(accessTokenString, refreshTokenString));
    }

    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );
        } catch (AuthenticationException e) {
            return Optional.empty();
        }
        var user = userService.findByEmail(request.email()).orElseThrow();
        return generateAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> refreshToken(String refreshTokenString) {
        return refreshTokenService.findByToken(refreshTokenString)
                .filter(token -> !token.isRevoked())
                .filter(refreshToken -> jwtService.isTokenValid(refreshTokenString, refreshToken.getUser()))
                .map(refreshToken -> {
                    UserDetails userDetails = refreshToken.getUser();

                    accessTokenService.revokeAllByRefreshToken(refreshToken);

                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    accessTokenService.createToken(userDetails.getUsername(), newAccessToken, refreshToken);

                    return new AuthenticationResponseDto(newAccessToken, refreshTokenString);
                });
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        verificationService.createAndSendToken(user, VerificationTokenPurpose.PASSWORD_RESET);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found"));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(token);
            throw new InvalidTokenException("Token has expired");
        }

        if (verificationToken.getPurpose() != VerificationTokenPurpose.PASSWORD_RESET) {
            throw new InvalidTokenException("Token is not for password reset");
        }

        User user = verificationToken.getUser();
        userService.changePassword(user, newPassword);

        verificationTokenService.revokeToken(token);
    }
}
