package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.MediaFile;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
