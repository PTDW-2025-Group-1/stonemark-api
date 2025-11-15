package pt.estga.stonemark.services.file;

import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.TargetType;

import java.io.IOException;

public interface MediaService {

    MediaFile save(MultipartFile file, TargetType targetType, Long targetId) throws IOException;

    void delete(Long mediaId);

}
