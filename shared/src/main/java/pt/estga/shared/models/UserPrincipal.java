package pt.estga.shared.models;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
public class UserPrincipal implements UserDetails, AuthenticatedPrincipal {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    @Override
    public Long getId() {
        return id;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword() {
        return password;
    }

    @Override public String getUsername() {
        return username;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return enabled;
    }

    @Override public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
}
