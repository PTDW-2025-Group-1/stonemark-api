package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import pt.estga.user.enums.Provider;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserIdentity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String identity;

    @ManyToOne
    @JoinColumn
    private User user;
}
