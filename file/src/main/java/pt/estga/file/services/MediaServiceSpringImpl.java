package pt.estga.file.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.StorageProvider;
import pt.estga.file.enums.TargetType;

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

    /**
     * Loads a file as a {@link Resource} from the given file path.
     *
     * @param filePath the path to the file to load
     * @return the loaded file as a {@link Resource}
     * @throws RuntimeException if the file cannot be loaded
     */
    @Override
    public Resource loadFile(String filePath) {
        return fileStorageService.loadFile(filePath);
    }
}
