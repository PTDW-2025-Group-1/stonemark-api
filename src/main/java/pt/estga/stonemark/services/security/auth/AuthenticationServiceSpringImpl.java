package pt.estga.stonemark.services.security.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
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
import pt.estga.stonemark.dtos.auth.*;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.exceptions.EmailAlreadyTakenException;
import pt.estga.stonemark.exceptions.EmailVerificationRequiredException;
import pt.estga.stonemark.mappers.UserMapper;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.AccessTokenService;
import pt.estga.stonemark.services.security.token.RefreshTokenService;
import pt.estga.stonemark.services.security.verification.VerificationProcessingService;
import pt.estga.stonemark.services.security.verification.commands.VerificationCommandFactory;
import pt.estga.stonemark.services.security.verification.VerificationInitiationService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
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
    private final VerificationInitiationService verificationInitiationService;
    private final VerificationCommandFactory verificationCommandFactory;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final VerificationProcessingService verificationProcessingService;

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
            throw new EmailAlreadyTakenException("email already in use");
        }

        User parsedUser = mapper.registerRequestToUser(request);
        parsedUser.setPassword(passwordEncoder.encode(request.password()));
        if (parsedUser.getRole() == null) {
            parsedUser.setRole(Role.USER);
        }

        parsedUser.setEnabled(!emailVerificationRequired);

        User user = userService.create(parsedUser);

        if (emailVerificationRequired) {
            var command = verificationCommandFactory.createEmailVerificationCommand(user);
            verificationInitiationService.initiate(command);
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

        if (emailVerificationRequired && !user.isEnabled()) {
            throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
        }

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
    public void requestPasswordReset(PasswordResetRequestDto request) {
        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var command = verificationCommandFactory.createPasswordResetCommand(user);
        verificationInitiationService.initiate(command);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto request) {
        verificationProcessingService.processPasswordReset(request.token(), request.newPassword());
    }


    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> authenticateWithGoogle(String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
            if (idToken == null) {
                return Optional.empty();
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();

            User user = userService.findByEmail(email)
                    .map(existingUser -> {
                        existingUser.setGoogleId(googleId);
                        return userService.update(existingUser);
                    })
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setFirstName((String) payload.get("given_name"));
                        newUser.setLastName((String) payload.get("family_name"));
                        newUser.setRole(Role.USER);
                        newUser.setEnabled(!emailVerificationRequired); // Set enabled based on config
                        newUser.setGoogleId(googleId);
                        return userService.create(newUser);
                    });

            if (emailVerificationRequired && !user.isEnabled()) {
                throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
            }

            return generateAuthenticationResponse(user);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error while authenticating with Google", e);
            return Optional.empty();
        }
    }

    @Override
    public void disconnectGoogle(User user) {
                if (user.getPassword() == null) {
            throw new IllegalStateException("You must set a password before disconnecting your Google account.");
        }
        user.setGoogleId(null);
        userService.update(user);
    }

    @Override
    public void logoutFromAllDevices(User user) {
        refreshTokenService.revokeAllByUser(user);
        accessTokenService.revokeAllByUser(user);
    }

    @Override
    public void logoutFromAllOtherDevices(User user, String currentToken) {
        List<RefreshToken> refreshTokens = refreshTokenService.findAllValidByUser(user);
        refreshTokens.removeIf(token -> token.getToken().equals(currentToken));
        refreshTokenService.revokeAll(refreshTokens);
        accessTokenService.revokeAllByUser(user);
    }
}
