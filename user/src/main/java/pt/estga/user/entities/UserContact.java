package pt.estga.user.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pt.estga.user.enums.ContactType;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserContact {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContactType type;

    private String value;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary;

    private Instant verifiedAt;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn
    private User user;
}
