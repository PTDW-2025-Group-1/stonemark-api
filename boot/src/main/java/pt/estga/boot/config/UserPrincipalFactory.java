package pt.estga.boot.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pt.estga.security.models.UserPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.enums.Role;

import java.util.Collection;
import java.util.List;

@Component
public class UserPrincipalFactory {

    public UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRole()),
                user.isEnabled(),
                !user.isAccountLocked()
        );
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
