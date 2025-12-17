package pt.estga.file.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.StorageProvider;
import pt.estga.shared.enums.TargetType;

import java.io.IOException;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceSpringImpl implements MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MediaFile save(byte[] fileData, String originalFilename, TargetType targetType) throws IOException {
        String directory = targetType.name().toLowerCase();
        String storagePath = fileStorageService.storeFile(fileData, originalFilename, directory);
        String filename = Paths.get(storagePath).getFileName().toString();

        MediaFile media = MediaFile.builder()
                .fileName(filename)
                .originalFileName(originalFilename)
                .size((long) fileData.length)
                .storageProvider(StorageProvider.MINIO)
                .storagePath(storagePath)
                .targetType(targetType)
                .build();

        return mediaFileRepository.save(media);
    }

    /**
     * Loads a file as a {@link Resource} from the given file path.
     *
     * @param filePath the path to the file to load
     * @return the loaded file as a {@link Resource}
     * @throws RuntimeException if the file cannot be loaded
     */
    @Override
    public Resource loadFile(String filePath) {
        log.info("Loading file: {}", filePath);
        return fileStorageService.loadFile(filePath);
    }
}
