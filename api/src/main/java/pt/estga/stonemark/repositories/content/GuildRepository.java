package pt.estga.stonemark.repositories.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.content.Guild;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
}
