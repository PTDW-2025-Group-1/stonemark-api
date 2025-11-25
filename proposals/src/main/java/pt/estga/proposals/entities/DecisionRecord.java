package pt.estga.proposals.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pt.estga.proposals.enums.DecisionType;
import pt.estga.user.entities.User;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DecisionRecord {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DecisionType decisionType;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseProposal baseProposal;

    private String comments;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

}
