package pt.estga.stonemark.services.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.repositories.MediaRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MediaServiceSpringImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MediaFile save(MultipartFile file, TargetType targetType, Long targetId) throws IOException {
        String directory = targetType.name().toLowerCase() + "/" + targetId;
        String storagePath = fileStorageService.storeFile(file, directory);

        MediaFile media = MediaFile.builder()
                .fileName(file.getOriginalFilename())
                .originalFileName(file.getOriginalFilename())
                .size(file.getSize())
                .storageProvider(StorageProvider.LOCAL)
                .storagePath(storagePath)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        return mediaRepository.save(media);
    }

    @Override
    public void delete(Long mediaId) {
        MediaFile media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));
        fileStorageService.deleteFile(media.getStoragePath());
        mediaRepository.delete(media);
    }
}
