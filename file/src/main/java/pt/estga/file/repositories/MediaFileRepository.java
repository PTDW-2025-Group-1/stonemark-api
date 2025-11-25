package pt.estga.file.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.file.entities.MediaFile;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
