package pt.estga.file.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.repositories.MediaFileRepository;
import pt.estga.file.services.MediaService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Endpoints for media files.")
public class MediaController {

    private final MediaService mediaService;
    private final MediaFileRepository mediaFileRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMediaById(@PathVariable Long id) {
        log.info("Request to get media with id: {}", id);

        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(id);

        if (mediaFileOptional.isEmpty()) {
            log.warn("Media not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }

        MediaFile mediaFile = mediaFileOptional.get();
        if (mediaFile.getStoragePath() == null || mediaFile.getStoragePath().isEmpty()) {
            log.warn("MediaFile with id {} has no storage path.", id);
            return ResponseEntity.notFound().build();
        }

        log.info("Found media file, loading from path: {}", mediaFile.getStoragePath());
        Resource resource = mediaService.loadFile(mediaFile.getStoragePath());

        MediaType mediaType = MediaTypeFactory.getMediaType(mediaFile.getOriginalFileName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        String extension = StringUtils.getFilenameExtension(mediaFile.getOriginalFileName());
        String filename = "stonemark-" + mediaFile.getId() + (extension != null ? "." + extension : "");
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition",
                        "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
