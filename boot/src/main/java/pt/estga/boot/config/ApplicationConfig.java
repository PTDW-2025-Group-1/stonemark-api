package pt.estga.boot.config;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.security.models.UserPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.enums.Role;
import pt.estga.user.services.UserService;

import java.util.Collection;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserService userService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            return new UserPrincipal(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    getAuthorities(user.getRole()),
                    user.isEnabled(),
                    !user.isAccountLocked()
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider
                = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PhoneNumberUtil phoneNumberUtil() {
        return PhoneNumberUtil.getInstance();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return switch (role) {
            case Role.ADMIN -> List.of(
                    () -> Role.USER.name(),
                    () -> Role.REVIEWER.name(),
                    () -> Role.MODERATOR.name(),
                    () -> Role.ADMIN.name()
            );
            case Role.MODERATOR -> List.of(
                    () -> Role.USER.name(),
                    () -> Role.REVIEWER.name(),
                    () -> Role.MODERATOR.name()
            );
            case Role.REVIEWER ->  List.of(
                    () -> Role.USER.name(),
                    () -> Role.REVIEWER.name()
            );
            case Role.USER -> List.of(
                    () -> Role.USER.name()
            );
        };
    }
}
