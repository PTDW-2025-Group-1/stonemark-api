package pt.estga.stonemark.services;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;

import java.util.List;

public interface MediaService {

    MediaFile attachMediaToMark(Long markId, MultipartFile file, boolean primaryImage, int sortOrder);

    @Transactional
    List<MediaFile> getMediaForMark(Long markId);

    void deleteMedia(Long mediaId);

}
