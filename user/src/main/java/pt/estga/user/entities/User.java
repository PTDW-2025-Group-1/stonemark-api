package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean accountLocked;
    @Builder.Default
    private boolean enabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TfaMethod tfaMethod = TfaMethod.NONE;
    private String tfaSecret;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserIdentity> identities = new ArrayList<>();

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
        return username;
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
        return accountLocked == user.accountLocked && enabled == user.enabled && Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && role == user.role && tfaMethod == user.tfaMethod && Objects.equals(tfaSecret, user.tfaSecret) && Objects.equals(createdAt, user.createdAt) && Objects.equals(contacts, user.contacts) && Objects.equals(identities, user.identities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, username, password, role, accountLocked, enabled, tfaMethod, tfaSecret, createdAt, contacts, identities);
    }
}
