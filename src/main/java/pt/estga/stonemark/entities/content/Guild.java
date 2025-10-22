package pt.estga.stonemark.entities.content;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Guild {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private Date foundedDate;
    private Date dissolvedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
