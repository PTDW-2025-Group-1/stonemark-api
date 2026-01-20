package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MarkDto;
import pt.estga.content.entities.Mark;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.services.MarkSearchService;
import pt.estga.content.services.MarkService;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/marks")
@RequiredArgsConstructor
@Tag(name = "Marks", description = "Endpoints for marks.")
public class MarkController {

    private final MarkService service;
    private final MarkMapper mapper;
    private final MediaService mediaService;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;

    @GetMapping
    public Page<MarkDto> getMarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/management")
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<MarkDto> getMarksManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAllManagement(pageable).map(mapper::toDto);
    }

    @GetMapping("/details")
    public Page<MarkDto> getDetailedMarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkDto> getMark(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkDto> createMark(
            @RequestPart("data") MarkDto markDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Mark mark = mapper.toEntity(markDto);

        if (markDto.coverId() != null) {
            mediaService.findById(markDto.coverId()).ifPresent(mark::setCover);
        }

        Mark createdMark = service.create(mark, file);
        MarkDto response = mapper.toDto(createdMark);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkDto> updateMark(
            @PathVariable Long id,
            @RequestPart("data") MarkDto markDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Mark existingMark = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Mark not found"));

        mapper.updateEntityFromDto(markDto, existingMark);

        if (markDto.coverId() != null) {
            mediaService.findById(markDto.coverId()).ifPresent(existingMark::setCover);
        }

        Mark updatedMark = service.update(existingMark, file);
        return ResponseEntity.ok(mapper.toDto(updatedMark));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMark(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkDto> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Mark updatedMark = service.updateCover(id, file);
        return ResponseEntity.ok(mapper.toDto(updatedMark));
    }

    @PostMapping(value = "/search/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> searchByImage(@RequestParam("file") MultipartFile file) throws IOException {
        DetectionResult detectionResult = detectionService.detect(file.getInputStream(), file.getOriginalFilename());

        if (!detectionResult.isMasonMark()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> similarMarkIds = markSearchService.searchMarks(detectionResult.embedding());
        return ResponseEntity.ok(similarMarkIds);
    }
}
