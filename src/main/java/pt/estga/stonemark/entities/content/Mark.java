package pt.estga.stonemark.entities.content;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Mark {
    @Id
    @GeneratedValue
    private Long id;
    private Long guildId;
    private Long monumentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
