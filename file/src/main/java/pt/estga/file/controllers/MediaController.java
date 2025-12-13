package pt.estga.file.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.file.services.MediaService;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Endpoints for media files.")
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/{directory}/{filename:.+}")
    public ResponseEntity<Resource> getMedia(@PathVariable String directory, @PathVariable String filename) {
        String filePath = directory + "/" + filename;
        log.info("Getting media with path: {}", filePath);
        Resource media = mediaService.loadFile(filePath);
        log.info("Media loaded successfully with path: {}", filePath);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + media.getFilename() + "\"")
                .body(media);
    }

}
