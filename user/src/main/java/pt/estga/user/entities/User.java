package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pt.estga.user.Role;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String password;

    private String googleId;
    private String telegramChatId;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean accountLocked;
    private boolean enabled;

    @CreationTimestamp
    private Instant createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return accountLocked == user.accountLocked && enabled == user.enabled && Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(email, user.email) && Objects.equals(telephone, user.telephone) && Objects.equals(password, user.password) && Objects.equals(googleId, user.googleId) && Objects.equals(telegramChatId, user.telegramChatId) && role == user.role && Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, telephone, password, googleId, telegramChatId, role, accountLocked, enabled, createdAt);
    }
}
