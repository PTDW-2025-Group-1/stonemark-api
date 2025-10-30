package pt.estga.stonemark.services.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;

    @Override
    @Transactional
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
        User user = userService.create(parsedUser);

        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return new AuthenticationResponseDto(accessTokenString, refreshTokenString);
    }

    @Override
    @Transactional
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );
        var user = userService.findByEmail(request.email()).orElseThrow();
        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return new AuthenticationResponseDto(accessTokenString, refreshTokenString);
    }

    @Override
    @Transactional
    public AuthenticationResponseDto refreshToken(String refreshTokenString) {
        return refreshTokenService.findByToken(refreshTokenString)
                .filter(token -> !token.isRevoked())
                .map(refreshToken -> {
                    UserDetails userDetails = refreshToken.getUser();
                    if (!jwtService.isTokenValid(refreshTokenString, userDetails)) {
                        throw new IllegalArgumentException("Refresh token is invalid");
                    }

                    accessTokenService.revokeAllByRefreshToken(refreshToken);

                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    accessTokenService.createToken(userDetails.getUsername(), newAccessToken, refreshToken);

                    return new AuthenticationResponseDto(newAccessToken, refreshTokenString);
                })
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found or revoked"));
    }
}
