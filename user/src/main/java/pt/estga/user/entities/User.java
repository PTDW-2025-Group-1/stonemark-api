package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserContact> contacts = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserIdentity> identities = new ArrayList<>();

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
