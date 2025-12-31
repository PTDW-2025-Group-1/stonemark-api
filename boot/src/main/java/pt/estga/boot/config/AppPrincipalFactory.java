package pt.estga.boot.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pt.estga.shared.enums.PrincipalType;
import pt.estga.shared.models.AppPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.enums.UserRole;

import java.util.Collection;
import java.util.List;

@Component
public class AppPrincipalFactory {

    public AppPrincipal fromLoginUser(User user) {
        return AppPrincipal.builder()
                .id(user.getId())
                .type(PrincipalType.USER)
                .identifier(user.getUsername())
                .password(user.getPassword())
                .authorities(mapUserRolesToAuthorities(user.getRole()))
                .enabled(user.isEnabled())
                .accountNonLocked(!user.isAccountLocked())
                .build();
    }

    public AppPrincipal fromJwtUser(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
        return AppPrincipal.builder()
                .id(id)
                .type(PrincipalType.USER)
                .identifier(username)
                .password(null)
                .authorities(authorities)
                .enabled(true) // optionally hardcode if already verified in token
                .accountNonLocked(true)
                .build();
    }

    public AppPrincipal fromJwtService(Long id, String serviceName, Collection<? extends GrantedAuthority> authorities) {
        return AppPrincipal.builder()
                .id(id)
                .type(PrincipalType.SERVICE)
                .identifier(serviceName)
                .password(null)
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    private Collection<? extends GrantedAuthority> mapUserRolesToAuthorities(UserRole role) {
        return switch (role) {
            case ADMIN -> List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name(),
                    () -> UserRole.MODERATOR.name(),
                    () -> UserRole.ADMIN.name()
            );
            case MODERATOR -> List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name(),
                    () -> UserRole.MODERATOR.name()
            );
            case REVIEWER -> List.of(
                    () -> UserRole.USER.name(),
                    () -> UserRole.REVIEWER.name()
            );
            case USER -> List.of(
                    () -> UserRole.USER.name()
            );
        };
    }
}
