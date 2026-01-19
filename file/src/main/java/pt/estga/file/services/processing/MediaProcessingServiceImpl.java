package pt.estga.file.services.processing;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.entities.MediaVariant;
import pt.estga.file.enums.MediaStatus;
import pt.estga.file.enums.MediaVariantType;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.repositories.MediaVariantRepository;
import pt.estga.file.services.FileStorageService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingServiceImpl implements MediaProcessingService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaVariantRepository mediaVariantRepository;
    private final FileStorageService fileStorageService;

    @PostConstruct
    public void verifyWebpSupport() {
        if (!ImageIO.getImageWritersByFormatName("webp").hasNext()) {
            throw new IllegalStateException(
                "WEBP ImageIO writer not available. Check classpath."
            );
        }
    }

    @Override
    public void process(Long mediaFileId) {
        log.info("Starting processing for media file ID: {}", mediaFileId);
        
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new RuntimeException("MediaFile not found: " + mediaFileId));

        try {
            mediaFile.setStatus(MediaStatus.PROCESSING);
            mediaFileRepository.save(mediaFile);

            // Determine if we should process this file
            String originalFilename = mediaFile.getOriginalFilename();
            if (!isSupportedImage(originalFilename)) {
                log.info("File {} is not a supported image for variants (JPEG/PNG only), skipping.", originalFilename);
                mediaFile.setStatus(MediaStatus.READY);
                mediaFileRepository.save(mediaFile);
                return;
            }

            // Load original file
            Resource resource = fileStorageService.loadFile(mediaFile.getStoragePath());
            File tempOriginalFile = Files.createTempFile("original-", originalFilename).toFile();
            
            try {
                try (InputStream is = resource.getInputStream();
                     OutputStream os = new FileOutputStream(tempOriginalFile)) {
                    is.transferTo(os);
                }

                // Generate variants
                generateVariant(mediaFile, tempOriginalFile, MediaVariantType.THUMBNAIL);
                generateVariant(mediaFile, tempOriginalFile, MediaVariantType.PREVIEW);
                generateVariant(mediaFile, tempOriginalFile, MediaVariantType.OPTIMIZED);

                mediaFile.setStatus(MediaStatus.READY);
                mediaFileRepository.save(mediaFile);
                log.info("Processing completed for media file ID: {}", mediaFileId);
            } finally {
                // Cleanup temp file
                if (tempOriginalFile.exists()) {
                    tempOriginalFile.delete();
                }
            }

        } catch (Exception e) {
            log.error("Processing failed for media file ID: {}", mediaFileId, e);
            mediaFile.setStatus(MediaStatus.FAILED);
            mediaFileRepository.save(mediaFile);
        }
    }

    private void generateVariant(MediaFile mediaFile, File originalFile, MediaVariantType type) throws IOException {
        if (mediaVariantRepository.existsByMediaFileAndType(mediaFile, type)) {
            log.info("Variant {} already exists for media file ID: {}, skipping.", type, mediaFile.getId());
            return;
        }

        File tempVariantFile = Files.createTempFile("variant-", ".webp").toFile();
        
        try {
            Thumbnails.Builder<File> builder = Thumbnails.of(originalFile);
            
            // Configure based on type
            switch (type) {
                case THUMBNAIL:
                    builder.size(200, 200).crop(net.coobird.thumbnailator.geometry.Positions.CENTER);
                    break;
                case PREVIEW:
                    builder.size(1024, 1024);
                    break;
                case OPTIMIZED:
                    builder.scale(1.0); // Keep original size
                    break;
            }

            builder.outputFormat("webp").toFile(tempVariantFile);

            // Store variant
            String variantPath = String.format("%d/derived/%s.webp", mediaFile.getId(), type.name().toLowerCase());
            String storagePath;
            try (InputStream is = new FileInputStream(tempVariantFile)) {
                storagePath = fileStorageService.storeFile(is, variantPath);
            }

            // Get dimensions efficiently
            int width = 0;
            int height = 0;
            try (ImageInputStream in = ImageIO.createImageInputStream(tempVariantFile)) {
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(in);
                        width = reader.getWidth(0);
                        height = reader.getHeight(0);
                    } finally {
                        reader.dispose();
                    }
                }
            }

            MediaVariant variant = MediaVariant.builder()
                    .mediaFile(mediaFile)
                    .type(type)
                    .storagePath(storagePath)
                    .width(width)
                    .height(height)
                    .size(tempVariantFile.length())
                    .build();
            
            mediaVariantRepository.save(variant);

        } finally {
            if (tempVariantFile.exists()) {
                tempVariantFile.delete();
            }
        }
    }

    private boolean isSupportedImage(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        // Only JPEG and PNG are supported for variant generation as per requirements
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }
}
