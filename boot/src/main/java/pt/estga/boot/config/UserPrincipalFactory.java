package pt.estga.boot.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pt.estga.shared.models.UserPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.enums.UserRole;

import java.util.Collection;
import java.util.List;

@Component
public class UserPrincipalFactory {

    public UserPrincipal create(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .enabled(user.isEnabled())
                .accountNonLocked(!user.isAccountLocked())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserRole role) {
        return switch (role) {
            case UserRole.ADMIN -> List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name(),
                    () -> UserRole.MODERATOR.name(),
                    () -> UserRole.ADMIN.name()
            );
            case UserRole.MODERATOR -> List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name(),
                    () -> UserRole.MODERATOR.name()
            );
            case UserRole.REVIEWER ->  List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name()
            );
            case UserRole.USER -> List.of(
                    () -> UserRole.USER.name()
            );
        };
    }
}
