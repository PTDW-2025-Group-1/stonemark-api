package pt.estga.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.content.entities.Guild;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
}
