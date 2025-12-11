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

    @Column(unique = true)
    private String value;

    private boolean isVerified;

    private boolean isPrimary;

    private Instant verifiedAt;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn
    private User user;
}
