package pt.estga.file.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.StorageProvider;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final FileStorageService fileStorageService;

    @Value("${storage.provider:local}")
    private String storageProvider;

    @Override
    @Transactional
    public MediaFile save(InputStream fileStream, String originalFilename) throws IOException {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // Create initial record with 0 size, will update after streaming
        MediaFile media = createInitialMediaFile(originalFilename);
        media = mediaFileRepository.save(media);

        String extension = StringUtils.getFilenameExtension(originalFilename);
        String newFilename = "stonemark-" + media.getId() + (extension != null ? "." + extension : "");
        media.setFilename(newFilename);

        // Todo: add target type start of relative path
        String normalizedFilename = newFilename.replace("\\", "/");
        String relativePath = String.format("%d/%s", media.getId(), normalizedFilename);

        CountingInputStream countingStream = new CountingInputStream(fileStream);
        String storagePath = fileStorageService.storeFile(countingStream, relativePath);

        media.setStoragePath(storagePath);
        media.setSize(countingStream.getCount());
        
        return mediaFileRepository.save(media);
    }

    private MediaFile createInitialMediaFile(String originalFilename) {
        return MediaFile.builder()
                .filename(originalFilename) // Temporarily set to original, will be updated
                .originalFilename(originalFilename)
                .size(0L)
                .storageProvider(StorageProvider.valueOf(storageProvider.toUpperCase()))
                .storagePath("")
                .build();
    }

    @Override
    public Resource loadFileById(Long fileId) {
        log.info("Loading file with ID: {}", fileId);
        MediaFile mediaFile = mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("MediaFile not found with id: " + fileId));
        return fileStorageService.loadFile(mediaFile.getStoragePath());
    }

    @Override
    public byte[] getMediaContent(Long fileId) {
        log.info("Getting content for file with ID: {}", fileId);
        try {
            Resource resource = loadFileById(fileId);
            return resource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Could not read file content for id: " + fileId, e);
        }
    }

    @Override
    public Optional<MediaFile> findById(Long id) {
        return mediaFileRepository.findById(id);
    }

    /**
     * Simple wrapper to count bytes read from an InputStream.
     */
    @Getter
    private static class CountingInputStream extends FilterInputStream {
        private long count;

        protected CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                count++;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result != -1) {
                count += result;
            }
            return result;
        }
    }
}
