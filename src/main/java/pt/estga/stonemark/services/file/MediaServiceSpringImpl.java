package pt.estga.stonemark.services.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.repositories.MediaFileRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MediaServiceSpringImpl implements MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MediaFile save(byte[] fileData, String filename, TargetType targetType) throws IOException {
        String directory = targetType.name().toLowerCase();
        String storagePath = fileStorageService.storeFile(fileData, filename, directory);

        MediaFile media = MediaFile.builder()
                .fileName(filename)
                .originalFileName(filename)
                .size((long) fileData.length)
                .storageProvider(StorageProvider.LOCAL)
                .storagePath(storagePath)
                .targetType(targetType)
                .build();

        return mediaFileRepository.save(media);
    }

    @Override
    public void delete(Long mediaId) {
        MediaFile media = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));
        fileStorageService.deleteFile(media.getStoragePath());
        mediaFileRepository.delete(media);
    }
}
