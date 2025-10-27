package pt.estga.stonemark.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.config.JwtService;
import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.mappers.UserMapper;
import pt.estga.stonemark.services.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;

    @Override
    public AuthenticationResponseDto register(RegisterRequestDto request) {
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
        User user = userService.save(parsedUser);

        var refreshToken = jwtService.generateAndSaveRefreshToken(user);
        var token = jwtService.generateAndSaveToken(user, refreshToken);

        return new AuthenticationResponseDto(token, refreshToken);
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );
        var user = userService.findByEmail(request.email()).orElseThrow();
        var refreshToken = jwtService.generateAndSaveRefreshToken(user);
        var token = jwtService.generateAndSaveToken(user, refreshToken);

        return new AuthenticationResponseDto(token, refreshToken);
    }

    @Override
    public AuthenticationResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            return null;
        }
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            return null;
        }
        var userOpt = this.userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return null;
        }
        var user = userOpt.get();
        if (!jwtService.isTokenValid(refreshToken, user)) {
            return null;
        }

        tokenService.revokeAllByRefreshToken(refreshToken);
        var accessToken = jwtService.generateAndSaveToken(user, refreshToken);
        return AuthenticationResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
