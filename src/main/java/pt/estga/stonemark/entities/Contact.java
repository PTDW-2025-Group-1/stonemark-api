package pt.estga.stonemark.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import pt.estga.stonemark.enums.ContactStatus;

import java.time.Instant;

@Entity
@Table(name = "contact_messages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @Enumerated(EnumType.STRING)
    private ContactStatus status = ContactStatus.PENDING;
}

