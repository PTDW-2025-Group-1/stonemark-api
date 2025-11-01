package pt.estga.stonemark.services.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.repositories.content.MarkRepository;
import pt.estga.stonemark.repositories.MediaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceSpringImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MarkRepository markRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MediaFile attachMediaToMark(Long markId, MultipartFile file, boolean primaryImage, int sortOrder) {
        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new RuntimeException("MarkOccurrence not found with id: " + markId));

        // Store the file (in local filesystem, S3, etc.)
        String directory = "marks/" + markId;
        String storagePath = fileStorageService.storeFile(file, directory);

        MediaFile media = MediaFile.builder()
            .fileName(file.getOriginalFilename())
            .originalFileName(file.getOriginalFilename())
            .size(file.getSize())
            .storageProvider(StorageProvider.LOCAL) // or AZURE if using cloud
            .storagePath(storagePath)
            .providerPublicId(null) // can be set by cloud storage (e.g., Azure blob ID)
            .targetType(TargetType.MARK)
            .targetId(mark.getId())
            .primaryImage(primaryImage)
            .sortOrder(sortOrder)
            .build();

        return mediaRepository.save(media);
    }

    @Override
    @Transactional
    public List<MediaFile> getMediaForMark(Long markId) {
        return mediaRepository.findByTargetTypeAndTargetId(TargetType.MARK, markId);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        MediaFile media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));
        fileStorageService.deleteFile(media.getStoragePath());
        mediaRepository.delete(media);
    }
}
