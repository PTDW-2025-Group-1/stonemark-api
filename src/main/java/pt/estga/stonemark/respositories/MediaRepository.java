package pt.estga.stonemark.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.TargetType;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<MediaFile, Long> {

    List<MediaFile> findByTargetTypeAndTargetId(TargetType targetType, Long markId);

}
