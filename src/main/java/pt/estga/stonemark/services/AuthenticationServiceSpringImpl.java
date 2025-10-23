package pt.estga.stonemark.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.config.JwtService;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.respositories.UserRepository;

@Service
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceSpringImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        var user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() != null) {
            user.setRole(request.role());
        } else {
            user.setRole(Role.USER);
        }
        repository.save(user);

        var token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token, user.getRole().name());
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = repository.findByEmail(request.email()).orElseThrow();
        var token = jwtService.generateToken(user);

        return new AuthenticationResponseDto(token, user.getRole().name());
    }

}
