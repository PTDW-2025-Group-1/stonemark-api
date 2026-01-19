package pt.estga.file.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.entities.MediaVariant;
import pt.estga.file.enums.MediaVariantType;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.repositories.MediaVariantRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaVariantServiceImpl implements MediaVariantService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaVariantRepository mediaVariantRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Resource loadVariant(Long mediaId, MediaVariantType type) {
        log.info("Loading variant {} for media ID: {}", type, mediaId);
        
        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("MediaFile not found with id: " + mediaId));

        MediaVariant variant = mediaVariantRepository.findByMediaFileAndType(mediaFile, type)
                .orElseThrow(() -> new RuntimeException("Variant not found: " + type));

        return fileStorageService.loadFile(variant.getStoragePath());
    }
}
