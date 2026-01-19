package pt.estga.file.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pt.estga.file.dtos.MediaFileDto;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.MediaVariantType;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.file.services.MediaService;
import pt.estga.file.services.MediaVariantService;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Endpoints for media files.")
public class MediaController {

    private final MediaService mediaService;
    private final MediaVariantService mediaVariantService;
    private final MediaFileMapper mediaFileMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFileDto> uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Request to upload media file: {}", file.getOriginalFilename());
        MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        return ResponseEntity.ok(mediaFileMapper.toDto(mediaFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMediaById(@PathVariable Long id) {
        log.info("Request to get media with id: {}", id);

        Optional<MediaFile> mediaFileOptional = mediaService.findById(id);

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
        Resource resource = mediaService.loadFileById(id);

        MediaType mediaType = MediaTypeFactory.getMediaType(mediaFile.getOriginalFilename())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        String extension = StringUtils.getFilenameExtension(mediaFile.getOriginalFilename());
        String filename = "stonemark-" + mediaFile.getId() + (extension != null ? "." + extension : "");
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition",
                        "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) {
        return getVariant(id, MediaVariantType.THUMBNAIL);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> getPreview(@PathVariable Long id) {
        return getVariant(id, MediaVariantType.PREVIEW);
    }

    @GetMapping("/{id}/optimized")
    public ResponseEntity<Resource> getOptimized(@PathVariable Long id) {
        return getVariant(id, MediaVariantType.OPTIMIZED);
    }

    private ResponseEntity<Resource> getVariant(Long id, MediaVariantType type) {
        try {
            Resource resource = mediaVariantService.loadVariant(id, type);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/webp"))
                    .body(resource);
        } catch (RuntimeException e) {
            // Assuming RuntimeException is thrown when not found as per service impl
            // We should ideally check for specific exception or catch generic and return 404
            // The requirement says "Variant missing -> 404", "Media missing -> 404"
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found", e);
        }
    }
}
